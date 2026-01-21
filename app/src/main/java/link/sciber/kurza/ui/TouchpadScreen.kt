package link.sciber.kurza.ui

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import link.sciber.kurza.bluetooth.BluetoothController
import link.sciber.kurza.ui.components.ConnectionPanel
import link.sciber.kurza.ui.components.MouseButtonsRow
import link.sciber.kurza.ui.components.StatusIndicator
import link.sciber.kurza.ui.components.DEFAULT_SCROLL_SENSITIVITY
import link.sciber.kurza.ui.components.DEFAULT_SENSITIVITY
import link.sciber.kurza.ui.components.MouseEvent
import link.sciber.kurza.ui.components.TouchpadArea
import link.sciber.kurza.ui.theme.StatusConnected
import link.sciber.kurza.ui.theme.StatusConnecting
import link.sciber.kurza.ui.theme.StatusDisconnected

@Composable
fun TouchpadScreen(controller: BluetoothController, activity: Activity) {
    val connectionState by controller.connectionState.collectAsState()
    val deviceName by controller.connectedDeviceName.collectAsState()
    val pairedDevices by controller.pairedDevices.collectAsState()
    val isHidReady by controller.isHidReady.collectAsState()
    val lastConnectedDevice by controller.lastConnectedDevice.collectAsState()
    val isBluetoothEnabled by controller.isBluetoothEnabled.collectAsState()

    var leftBtnPressed by remember { mutableStateOf(false) }
    var rightBtnPressed by remember { mutableStateOf(false) }

    TouchpadScreenContent(
        connectionState = connectionState,
        deviceName = deviceName,
        isHidReady = isHidReady,
        isBluetoothEnabled = isBluetoothEnabled,
        lastConnectedDevice = lastConnectedDevice,
        pairedDevices = pairedDevices,
        leftBtnPressed = leftBtnPressed,
        rightBtnPressed = rightBtnPressed,
        onLeftBtnPressedChange = { leftBtnPressed = it },
        onRightBtnPressedChange = { rightBtnPressed = it },
        onReconnect = { controller.reconnect() },
        onMakeDiscoverable = { controller.requestDiscoverable(activity) },
        onRequestEnableBluetooth = { controller.requestEnableBluetooth(activity) },
        onConnectToDevice = { controller.connectToDevice(it) },
        onMouseEvent = { event ->
            controller.sendMouse(event.dx, event.dy, event.scroll, event.leftButton, event.rightButton)
        }
    )
}

@Composable
fun TouchpadScreenContent(
    connectionState: Int,
    deviceName: String?,
    isHidReady: Boolean,
    isBluetoothEnabled: Boolean,
    lastConnectedDevice: BluetoothDevice?,
    pairedDevices: List<BluetoothDevice>,
    leftBtnPressed: Boolean,
    rightBtnPressed: Boolean,
    sensitivity: Float = DEFAULT_SENSITIVITY,
    scrollSensitivity: Float = DEFAULT_SCROLL_SENSITIVITY,
    onLeftBtnPressedChange: (Boolean) -> Unit,
    onRightBtnPressedChange: (Boolean) -> Unit,
    onReconnect: () -> Unit,
    onMakeDiscoverable: () -> Unit,
    onRequestEnableBluetooth: () -> Unit,
    onConnectToDevice: (BluetoothDevice) -> Unit,
    onMouseEvent: (MouseEvent) -> Unit
) {
    val statusText = when(connectionState) {
        BluetoothProfile.STATE_CONNECTED -> deviceName ?: "PC"
        BluetoothProfile.STATE_CONNECTING -> "Connecting..."
        BluetoothProfile.STATE_DISCONNECTING -> "Disconnecting..."
        else -> "Not connected"
    }

    val statusColor by animateColorAsState(
        targetValue = when(connectionState) {
            BluetoothProfile.STATE_CONNECTED -> StatusConnected
            BluetoothProfile.STATE_CONNECTING -> StatusConnecting
            else -> StatusDisconnected
        },
        animationSpec = tween(300),
        label = "statusColor"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Status indicator
            StatusIndicator(
                statusText = statusText,
                statusColor = statusColor
            )

            // Connection panel (when disconnected)
            if (connectionState != BluetoothProfile.STATE_CONNECTED) {
                Spacer(modifier = Modifier.height(16.dp))
                ConnectionPanel(
                    isBluetoothEnabled = isBluetoothEnabled,
                    isHidReady = isHidReady,
                    lastConnectedDevice = lastConnectedDevice,
                    pairedDevices = pairedDevices,
                    onReconnect = onReconnect,
                    onMakeDiscoverable = onMakeDiscoverable,
                    onRequestEnableBluetooth = onRequestEnableBluetooth,
                    onConnectToDevice = onConnectToDevice
                )
                Spacer(modifier = Modifier.weight(1f))
            } else {
                // Touchpad and buttons only when connected
                Spacer(modifier = Modifier.height(20.dp))

                TouchpadArea(
                    modifier = Modifier.weight(1f),
                    leftBtnPressed = leftBtnPressed,
                    rightBtnPressed = rightBtnPressed,
                    sensitivity = sensitivity,
                    scrollSensitivity = scrollSensitivity,
                    onMouseEvent = onMouseEvent
                )

                Spacer(modifier = Modifier.height(20.dp))

                MouseButtonsRow(
                    leftBtnPressed = leftBtnPressed,
                    rightBtnPressed = rightBtnPressed,
                    onLeftBtnPressedChange = onLeftBtnPressedChange,
                    onRightBtnPressedChange = onRightBtnPressedChange,
                    onMouseEvent = onMouseEvent
                )
            }
        }
    }
}
