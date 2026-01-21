package io.github.patrikflorek.kurza.ui

import android.bluetooth.BluetoothProfile
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.patrikflorek.kurza.ui.theme.KurzaTheme

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun TouchpadScreenConnectedPreview() {
    KurzaTheme {
        TouchpadScreenContent(
            connectionState = BluetoothProfile.STATE_CONNECTED,
            deviceName = "Desktop-PC",
            isHidReady = true,
            isBluetoothEnabled = true,
            lastConnectedDevice = null,
            pairedDevices = emptyList(),
            leftBtnPressed = false,
            rightBtnPressed = false,
            onLeftBtnPressedChange = {},
            onRightBtnPressedChange = {},
            onReconnect = {},
            onMakeDiscoverable = {},
            onRequestEnableBluetooth = {},
            onConnectToDevice = {},
            onMouseEvent = {}
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
            isBluetoothEnabled = true,
            lastConnectedDevice = null,
            pairedDevices = emptyList(),
            leftBtnPressed = false,
            rightBtnPressed = false,
            onLeftBtnPressedChange = {},
            onRightBtnPressedChange = {},
            onReconnect = {},
            onMakeDiscoverable = {},
            onRequestEnableBluetooth = {},
            onConnectToDevice = {},
            onMouseEvent = {}
        )
    }
}
