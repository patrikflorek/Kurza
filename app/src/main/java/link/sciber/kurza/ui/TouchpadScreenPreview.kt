package link.sciber.kurza.ui

import android.bluetooth.BluetoothProfile
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import link.sciber.kurza.ui.theme.KurzaTheme

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun TouchpadScreenConnectedPreview() {
    KurzaTheme {
        TouchpadScreenContent(
            connectionState = BluetoothProfile.STATE_CONNECTED,
            deviceName = "Desktop-PC",
            isHidReady = true,
            lastConnectedDevice = null,
            pairedDevices = emptyList(),
            leftBtnPressed = false,
            rightBtnPressed = false,
            onLeftBtnPressedChange = {},
            onRightBtnPressedChange = {},
            onReconnect = {},
            onMakeDiscoverable = {},
            onConnectToDevice = {},
            onSendMouse = { _, _, _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun TouchpadScreenDisconnectedPreview() {
    KurzaTheme {
        TouchpadScreenContent(
            connectionState = BluetoothProfile.STATE_DISCONNECTED,
            deviceName = null,
            isHidReady = true,
            lastConnectedDevice = null,
            pairedDevices = emptyList(),
            leftBtnPressed = false,
            rightBtnPressed = false,
            onLeftBtnPressedChange = {},
            onRightBtnPressedChange = {},
            onReconnect = {},
            onMakeDiscoverable = {},
            onConnectToDevice = {},
            onSendMouse = { _, _, _, _, _ -> }
        )
    }
}
