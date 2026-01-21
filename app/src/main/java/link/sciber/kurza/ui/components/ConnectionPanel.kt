package link.sciber.kurza.ui.components

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import link.sciber.kurza.bluetooth.safeDisplayName
import link.sciber.kurza.ui.theme.KurzaTheme
import link.sciber.kurza.ui.theme.StatusConnecting

@Composable
fun ConnectionPanel(
    isBluetoothEnabled: Boolean,
    isHidReady: Boolean,
    lastConnectedDevice: BluetoothDevice?,
    pairedDevices: List<BluetoothDevice>,
    onReconnect: () -> Unit,
    onMakeDiscoverable: () -> Unit,
    onRequestEnableBluetooth: () -> Unit,
    onConnectToDevice: (BluetoothDevice) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                !isBluetoothEnabled -> {
                    Text(
                        text = "Bluetooth is turned off",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Turn on Bluetooth to continue using Kurza as a touchpad.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onRequestEnableBluetooth,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Turn on Bluetooth",
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
                !isHidReady -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(top = 4.dp),
                            strokeWidth = 3.dp
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Initializing Bluetooth HID...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = StatusConnecting
                            )
                            Text(
                                text = "This typically takes a couple of seconds after Bluetooth turns on.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> {
                    // Reconnect button
                    if (lastConnectedDevice != null) {
                        Button(
                            onClick = onReconnect,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Reconnect to ${lastConnectedDevice.safeDisplayName()}",
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    // Instructions
                    Text(
                        text = "First time? Unpair this phone from PC, tap 'Make Discoverable', then pair from PC.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )

                    OutlinedButton(
                        onClick = onMakeDiscoverable,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Make Discoverable",
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    if (pairedDevices.isNotEmpty()) {
                        Text(
                            text = "Paired Devices",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        pairedDevices.forEach { device ->
                            FilledTonalButton(
                                onClick = { onConnectToDevice(device) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = device.safeDisplayName(),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
fun ConnectionPanelPreview() {
    KurzaTheme {
        ConnectionPanel(
            isBluetoothEnabled = true,
            isHidReady = true,
            lastConnectedDevice = null,
            pairedDevices = emptyList(),
            onReconnect = {},
            onMakeDiscoverable = {},
            onRequestEnableBluetooth = {},
            onConnectToDevice = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
fun ConnectionPanelLoadingPreview() {
    KurzaTheme {
        ConnectionPanel(
            isBluetoothEnabled = true,
            isHidReady = false,
            lastConnectedDevice = null,
            pairedDevices = emptyList(),
            onReconnect = {},
            onMakeDiscoverable = {},
            onRequestEnableBluetooth = {},
            onConnectToDevice = {}
        )
    }
}
