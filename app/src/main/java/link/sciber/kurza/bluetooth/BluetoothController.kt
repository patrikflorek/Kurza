package link.sciber.kurza.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BluetoothController(private val context: Context) {

    private val appContext = context.applicationContext

    private val _connectionState = MutableStateFlow(BluetoothProfile.STATE_DISCONNECTED)
    val connectionState: StateFlow<Int> = _connectionState.asStateFlow()

    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    val connectedDeviceName: StateFlow<String?> = _connectedDeviceName.asStateFlow()

    private var hidService: HidService? = null
    private var bluetoothAdapter: BluetoothAdapter? = null

    private val _pairedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDevice>> = _pairedDevices.asStateFlow()

    private val _isHidReady = MutableStateFlow(false)
    val isHidReady: StateFlow<Boolean> = _isHidReady.asStateFlow()

    private val _lastConnectedDevice = MutableStateFlow<BluetoothDevice?>(null)
    val lastConnectedDevice: StateFlow<BluetoothDevice?> = _lastConnectedDevice.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(false)
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    private var pendingAutoReconnect = false
    private var receiverRegistered = false

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != BluetoothAdapter.ACTION_STATE_CHANGED) return
            when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                BluetoothAdapter.STATE_TURNING_OFF, BluetoothAdapter.STATE_OFF -> {
                    _isBluetoothEnabled.value = false
                    _isHidReady.value = false
                    _connectionState.value = BluetoothProfile.STATE_DISCONNECTED
                    _pairedDevices.value = emptyList()
                    if (_lastConnectedDevice.value != null) {
                        pendingAutoReconnect = true
                    }
                    hidService?.disconnect()
                    hidService = null
                    bluetoothAdapter = null
                }
                BluetoothAdapter.STATE_ON -> {
                    _isBluetoothEnabled.value = true
                    _isHidReady.value = false
                    initialize()
                }
            }
        }
    }

    fun start() {
        registerBluetoothReceiver()
        initialize()
    }

    fun stop() {
        if (receiverRegistered) {
            appContext.unregisterReceiver(bluetoothStateReceiver)
            receiverRegistered = false
        }
        hidService?.disconnect()
        hidService = null
        bluetoothAdapter = null
        _isHidReady.value = false
        _isBluetoothEnabled.value = false
        _pairedDevices.value = emptyList()
        _connectionState.value = BluetoothProfile.STATE_DISCONNECTED
    }

    fun initialize() {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = manager.adapter

        bluetoothAdapter = adapter
        val enabled = adapter?.isEnabled == true
        _isBluetoothEnabled.value = enabled

        if (adapter == null) {
            _isHidReady.value = false
            _pairedDevices.value = emptyList()
            return
        }

        if (!enabled) {
            _isHidReady.value = false
            return
        }

        if (hidService == null) {
            hidService = HidService(appContext, onHidReady = { ready ->
                _isHidReady.value = ready
                if (ready && pendingAutoReconnect) {
                    pendingAutoReconnect = false
                    reconnect()
                }
            }) { state, device ->
                _connectionState.value = state
                when (state) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        _lastConnectedDevice.value = device
                        _connectedDeviceName.value = safeDeviceName(device)
                    }
                    BluetoothProfile.STATE_CONNECTING -> {
                        _connectedDeviceName.value = safeDeviceName(device)
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        refreshPairedDevices()
                    }
                }
            }
        }

        refreshPairedDevices()
        hidService?.initialize(adapter)
    }

    fun connectToDevice(device: BluetoothDevice) {
        hidService?.connect(device)
    }

    fun sendMouse(dx: Int, dy: Int, wheel: Int, left: Boolean, right: Boolean) {
        hidService?.sendMouseReport(dx, dy, wheel, left, right, false)
    }

    fun disconnect() {
        hidService?.disconnect()
    }

    fun requestDiscoverable(activity: Activity, durationSeconds: Int = 300) {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, durationSeconds)
        }
        activity.startActivity(discoverableIntent)
    }

    fun requestEnableBluetooth(activity: Activity) {
        val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activity.startActivity(enableIntent)
    }

    fun reconnect() {
        _lastConnectedDevice.value?.let { device ->
            connectToDevice(device)
        }
    }

    private fun refreshPairedDevices() {
        try {
            _pairedDevices.value = bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    fun onResume() {
        val hasLastDevice = _lastConnectedDevice.value != null
        val isDisconnected = _connectionState.value == BluetoothProfile.STATE_DISCONNECTED

        if (hasLastDevice && isDisconnected && _isBluetoothEnabled.value) {
            pendingAutoReconnect = true
        }

        initialize()

        if (_isBluetoothEnabled.value) {
            hidService?.reInitialize()
            refreshPairedDevices()
        }
    }

    private fun registerBluetoothReceiver() {
        if (receiverRegistered) return
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        appContext.registerReceiver(bluetoothStateReceiver, filter)
        receiverRegistered = true
    }

    private fun safeDeviceName(device: BluetoothDevice?): String? = try {
        device?.name
    } catch (e: SecurityException) {
        "Unknown Device"
    }
}
