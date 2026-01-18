package link.sciber.kurza.ui

import android.bluetooth.BluetoothProfile
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.changedToUp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.Activity
import link.sciber.kurza.bluetooth.BluetoothController

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TouchpadScreen(controller: BluetoothController, activity: Activity) {
    val connectionState by controller.connectionState.collectAsState()
    val deviceName by controller.connectedDeviceName.collectAsState()
    val pairedDevices by controller.pairedDevices.collectAsState()
    val isHidReady by controller.isHidReady.collectAsState()
    val lastConnectedDevice by controller.lastConnectedDevice.collectAsState()

    var leftBtnPressed by remember { mutableStateOf(false) }
    var rightBtnPressed by remember { mutableStateOf(false) }
    
    val statusText = when(connectionState) {
        BluetoothProfile.STATE_CONNECTED -> "Connected: ${deviceName ?: "PC"}"
        BluetoothProfile.STATE_CONNECTING -> "Connecting to ${deviceName ?: "..."}..."
        BluetoothProfile.STATE_DISCONNECTING -> "Disconnecting..."
        else -> "Disconnected"
    }

    val statusColor = when(connectionState) {
        BluetoothProfile.STATE_CONNECTED -> Color.Green
        BluetoothProfile.STATE_CONNECTING -> Color.Yellow
        else -> Color.Red
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // Dark BG
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kurza Remote",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = statusText,
                color = statusColor,
                fontSize = 14.sp
            )
        }
        
        if (connectionState == BluetoothProfile.STATE_DISCONNECTED) {
            Spacer(modifier = Modifier.height(16.dp))
            
            if (!isHidReady) {
                Text("HID service not ready. Please wait...", color = Color.Yellow, fontSize = 12.sp)
            } else {
                // Show reconnect button if we have a last connected device
                if (lastConnectedDevice != null) {
                    val lastDeviceName = try { lastConnectedDevice?.name ?: "Last Device" } catch(e: SecurityException) { "Last Device" }
                    Button(
                        onClick = { controller.reconnect() },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text("Reconnect to $lastDeviceName")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                Text(
                    text = "First time? Unpair this phone from PC, tap 'Make Discoverable', then pair from PC.",
                    color = Color.Cyan,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { controller.requestDiscoverable(activity) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text("Make Discoverable (for new pairing)")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Or tap a paired device to connect:", color = Color.Gray, fontSize = 12.sp)
                pairedDevices.forEach { device ->
                    Button(
                        onClick = { controller.connectToDevice(device) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(text = try { device.name ?: "Unknown" } catch(e: SecurityException) { "Unknown" })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Touchpad Area with custom gesture detection
        val scope = rememberCoroutineScope()
        var lastTapTime by remember { mutableStateOf(0L) }
        val doubleTapTimeout = 300L // ms
        val sensitivity = 1.5f
        val scrollSensitivity = 0.3f
        
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF222222), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val firstDown = awaitFirstDown()
                        firstDown.consume()
                        
                        var lastPosition = firstDown.position
                        var isTwoFinger = false
                        var hasMoved = false
                        var lastTwoFingerCenter = Offset.Zero
                        val moveThreshold = 10f // pixels to consider it a drag vs tap
                        
                        while (true) {
                            val event = awaitPointerEvent()
                            val activePointers = event.changes.filter { it.pressed }
                            
                            if (activePointers.isEmpty()) {
                                // All fingers lifted
                                if (!hasMoved && !isTwoFinger) {
                                    // It was a tap (no significant movement, single finger)
                                    val currentTime = System.currentTimeMillis()
                                    if (currentTime - lastTapTime < doubleTapTimeout) {
                                        // Double tap = right click
                                        controller.sendMouse(0, 0, 0, false, true)
                                        controller.sendMouse(0, 0, 0, false, false)
                                        lastTapTime = 0L // Reset to prevent triple-tap
                                    } else {
                                        // Single tap = left click (delayed to check for double tap)
                                        lastTapTime = currentTime
                                        scope.launch {
                                            delay(doubleTapTimeout)
                                            if (lastTapTime == currentTime) {
                                                // No second tap occurred, send left click
                                                controller.sendMouse(0, 0, 0, true, false)
                                                controller.sendMouse(0, 0, 0, false, false)
                                            }
                                        }
                                    }
                                }
                                event.changes.forEach { it.consume() }
                                break
                            }
                            
                            if (activePointers.size >= 2) {
                                // Two finger gesture - scroll
                                isTwoFinger = true
                                val center = Offset(
                                    activePointers.map { it.position.x }.average().toFloat(),
                                    activePointers.map { it.position.y }.average().toFloat()
                                )
                                
                                if (lastTwoFingerCenter != Offset.Zero) {
                                    val deltaY = center.y - lastTwoFingerCenter.y
                                    val scrollAmount = (-deltaY * scrollSensitivity).toInt()
                                    if (scrollAmount != 0) {
                                        controller.sendMouse(0, 0, scrollAmount, leftBtnPressed, rightBtnPressed)
                                    }
                                }
                                lastTwoFingerCenter = center
                            } else if (activePointers.size == 1 && !isTwoFinger) {
                                // Single finger drag - cursor movement
                                val currentPosition = activePointers.first().position
                                val delta = currentPosition - lastPosition
                                
                                if (delta.getDistance() > moveThreshold) {
                                    hasMoved = true
                                }
                                
                                if (hasMoved) {
                                    controller.sendMouse(
                                        (delta.x * sensitivity).toInt(),
                                        (delta.y * sensitivity).toInt(),
                                        0,
                                        leftBtnPressed,
                                        rightBtnPressed
                                    )
                                }
                                lastPosition = currentPosition
                            }
                            
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
        ) {
            Text(
                text = "Touchpad Area\n\nTap = Left Click\nDouble Tap = Right Click\nTwo Fingers = Scroll",
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Center),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Mouse Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Button
            Button(
                onClick = {}, /* Handled by press interaction ideally, simple click for now */
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.changes.any { it.pressed }) {
                                    if (!leftBtnPressed) {
                                        leftBtnPressed = true
                                        controller.sendMouse(0, 0, 0, true, rightBtnPressed)
                                    }
                                } else {
                                    if (leftBtnPressed) {
                                        leftBtnPressed = false
                                        controller.sendMouse(0, 0, 0, false, rightBtnPressed)
                                    }
                                }
                            }
                        }
                    }
            ) {
                Text("L")
            }

            // Right Button
            Button(
                onClick = {},
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.changes.any { it.pressed }) {
                                    if (!rightBtnPressed) {
                                        rightBtnPressed = true
                                        controller.sendMouse(0, 0, 0, leftBtnPressed, true)
                                    }
                                } else {
                                    if (rightBtnPressed) {
                                        rightBtnPressed = false
                                        controller.sendMouse(0, 0, 0, leftBtnPressed, false)
                                    }
                                }
                            }
                        }
                    }
            ) {
                Text("R")
            }
        }
    }
}
