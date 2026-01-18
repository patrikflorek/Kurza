package link.sciber.kurza.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BluetoothController(private val context: Context) {

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

    private var pendingAutoReconnect = false

    fun initialize() {
        if (hidService != null) return

        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = manager.adapter

        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            return
        }
        
        refreshPairedDevices()

        hidService = HidService(context, onHidReady = { ready ->
            _isHidReady.value = ready
            // Auto-reconnect when HID becomes ready after resume
            if (ready && pendingAutoReconnect) {
                pendingAutoReconnect = false
                reconnect()
            }
        }) { state, device ->
            _connectionState.value = state
            if (state == BluetoothProfile.STATE_CONNECTED) {
                _lastConnectedDevice.value = device
                _connectedDeviceName.value = try {
                    device?.name
                } catch (e: SecurityException) {
                    "Unknown Device"
                }
            } else if (state == BluetoothProfile.STATE_CONNECTING) {
                _connectedDeviceName.value = try {
                    device?.name
                } catch (e: SecurityException) {
                    "Unknown Device"
                }
            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                refreshPairedDevices()
            }
        }
        hidService?.initialize(bluetoothAdapter!!)
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
        
        // Re-initialize HID service if it doesn't exist
        if (hidService == null) {
            if (hasLastDevice && isDisconnected) {
                pendingAutoReconnect = true
            }
            initialize()
            return
        }
        
        // Refresh paired devices list
        refreshPairedDevices()
        
        // Set up auto-reconnect if we have a last device and are disconnected
        if (hasLastDevice && isDisconnected) {
            pendingAutoReconnect = true
        }
        
        // Always re-acquire the profile proxy on resume to ensure it's fresh
        // This handles cases where the proxy became stale while app was in background
        // Calling getProfileProxy multiple times is safe - it will just trigger
        // onServiceConnected again which re-registers the HID app
        hidService?.reInitialize()
    }
}
