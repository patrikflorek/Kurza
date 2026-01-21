package io.github.patrikflorek.kurza.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppQosSettings
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log

class HidService(
    private val context: Context,
    private val onHidReady: (Boolean) -> Unit,
    private val onConnectionStateChanged: (Int, BluetoothDevice?) -> Unit
) {
    companion object {
        // QoS Settings for Bluetooth HID
        private const val QOS_TOKEN_RATE = 800          // Tokens per second for data transmission
        private const val QOS_TOKEN_BUCKET_SIZE = 900   // Max burst size in tokens
        private const val QOS_PEAK_BANDWIDTH = 10000    // Peak bandwidth in bytes/sec
        private const val QOS_LATENCY = 10000           // Max acceptable latency in microseconds
        private const val QOS_DELAY_VARIATION = 10000   // Max delay variation in microseconds
    }

    private var hidDevice: BluetoothHidDevice? = null
    private var connectedDevice: BluetoothDevice? = null
    private var bluetoothAdapter: BluetoothAdapter? = null

    // Standard Mouse Report Descriptor
    // 3 buttons + Wheel
    private val mouseReportDescriptor = byteArrayOf(
        0x05, 0x01,         // Usage Page (Generic Desktop)
        0x09, 0x02,         // Usage (Mouse)
        0xA1.toByte(), 0x01,// Collection (Application)
        0x09, 0x01,         //   Usage (Pointer)
        0xA1.toByte(), 0x00,//   Collection (Physical)
        0x05, 0x09,         //     Usage Page (Buttons)
        0x19, 0x01,         //     Usage Minimum (1)
        0x29, 0x03,         //     Usage Maximum (3)
        0x15, 0x00,         //     Logical Minimum (0)
        0x25, 0x01,         //     Logical Maximum (1)
        0x95.toByte(), 0x03,//     Report Count (3)
        0x75, 0x01,         //     Report Size (1)
        0x81.toByte(), 0x02,//     Input (Data, Variable, Absolute)
        0x95.toByte(), 0x01,//     Report Count (1)
        0x75, 0x05,         //     Report Size (5)
        0x81.toByte(), 0x03,//     Input (Constant) -> Padding
        0x05, 0x01,         //     Usage Page (Generic Desktop)
        0x09, 0x30,         //     Usage (X)
        0x09, 0x31,         //     Usage (Y)
        0x09, 0x38,         //     Usage (Wheel)
        0x15, 0x81.toByte(),//     Logical Minimum (-127)
        0x25, 0x7F,         //     Logical Maximum (127)
        0x75, 0x08,         //     Report Size (8)
        0x95.toByte(), 0x03,//     Report Count (3)
        0x81.toByte(), 0x06,//     Input (Data, Variable, Relative)
        0xC0.toByte(),      //   End Collection
        0xC0.toByte()       // End Collection
    )

    @SuppressLint("MissingPermission")
    private val inputHostCallback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            Log.d("HidService", "onAppStatusChanged: registered=$registered device=$pluggedDevice")
            onHidReady(registered)
        }

        override fun onSetReport(device: BluetoothDevice?, type: Byte, id: Byte, data: ByteArray?) {
            Log.d("HidService", "onSetReport: type=$type id=$id")
            // We must reply to keep connection alive
            hidDevice?.replyReport(device, BluetoothHidDevice.REPORT_TYPE_INPUT, id,  byteArrayOf())
        }
        
        override fun onGetReport(device: BluetoothDevice?, type: Byte, id: Byte, bufferSize: Int) {
            Log.d("HidService", "onGetReport: type=$type id=$id")
             // We must reply with valid data length matching descriptor (4 bytes for our mouse)
            hidDevice?.replyReport(device, BluetoothHidDevice.REPORT_TYPE_INPUT, id, ByteArray(4))
        }

        override fun onSetProtocol(device: BluetoothDevice?, protocol: Byte) {
            Log.d("HidService", "onSetProtocol: protocol=$protocol")
             // We must reply to keep connection alive - Protocol 0 = Boot, 1 = Report
            hidDevice?.replyReport(device, BluetoothHidDevice.REPORT_TYPE_INPUT, 0, byteArrayOf())
        }

        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            Log.d("HidService", "onConnectionStateChanged: state=$state device=$device")
            if (state == BluetoothProfile.STATE_CONNECTED) {
                connectedDevice = device
            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                connectedDevice = null
            }
            onConnectionStateChanged(state, device)
        }
    }

    private val serviceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = proxy as BluetoothHidDevice
                registerApp()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = null
                onHidReady(false)
            }
        }
    }

    fun initialize(bluetoothAdapter: BluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter
        bluetoothAdapter.getProfileProxy(context, serviceListener, BluetoothProfile.HID_DEVICE)
    }

    fun reInitialize() {
        // Re-acquire profile proxy if we have an adapter
        bluetoothAdapter?.let { adapter ->
            adapter.getProfileProxy(context, serviceListener, BluetoothProfile.HID_DEVICE)
        }
    }

    @SuppressLint("MissingPermission")
    private fun registerApp() {
        val sdpSettings = BluetoothHidDeviceAppSdpSettings(
            "Kurza Remote",
            "Remote touchpad",
            "Android",
            0x80.toByte(), // Subclass: Mouse
            mouseReportDescriptor
        )

        val qosSettings = BluetoothHidDeviceAppQosSettings(
            BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT,
            QOS_TOKEN_RATE,
            QOS_TOKEN_BUCKET_SIZE,
            QOS_PEAK_BANDWIDTH,
            QOS_LATENCY,
            QOS_DELAY_VARIATION
        )

        hidDevice?.registerApp(
            sdpSettings,
            null,
            qosSettings,
             // Use main executor for simplicity in this demo, or immediate
            { it.run() }, 
            inputHostCallback
        )
    }

    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice) {
        hidDevice?.connect(device)
    }

    @SuppressLint("MissingPermission")
    fun sendMouseReport(dx: Int, dy: Int, wheel: Int, leftBtn: Boolean, rightBtn: Boolean, middleBtn: Boolean) {
        val device = connectedDevice ?: return
        val service = hidDevice ?: return

        // Byte layout from descriptor:
        // Byte 0: Buttons (Bit 0: Left, Bit 1: Right, Bit 2: Middle)
        // Byte 1: X (Relative)
        // Byte 2: Y (Relative)
        // Byte 3: Wheel (Relative)

        var buttons = 0
        if (leftBtn) buttons = buttons or 1
        if (rightBtn) buttons = buttons or 2
        if (middleBtn) buttons = buttons or 4

        val report = ByteArray(4)
        report[0] = buttons.toByte()
        report[1] = dx.coerceIn(-127, 127).toByte()
        report[2] = dy.coerceIn(-127, 127).toByte()
        report[3] = wheel.coerceIn(-127, 127).toByte()

        service.sendReport(device, 0, report)
    }
    
    @SuppressLint("MissingPermission")
    fun disconnect() {
        hidDevice?.unregisterApp()
        // Note: can't easily force disconnect from app side other than unregistering
    }
}
