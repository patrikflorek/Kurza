package link.sciber.kurza.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import link.sciber.kurza.ui.theme.KurzaTheme

@Composable
fun TouchpadArea(
    modifier: Modifier = Modifier,
    leftBtnPressed: Boolean,
    rightBtnPressed: Boolean,
    onSendMouse: (Int, Int, Int, Boolean, Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    var lastTapTime by remember { mutableStateOf(0L) }
    val doubleTapTimeout = 300L
    val sensitivity = 1.5f
    val scrollSensitivity = 0.3f

    // Visual feedback state
    var isTouching by remember { mutableStateOf(false) }
    var touchPosition by remember { mutableStateOf(Offset.Zero) }
    var fingerCount by remember { mutableStateOf(0) }

    val borderColor by animateColorAsState(
        targetValue = if (isTouching)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        else
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        animationSpec = tween(150),
        label = "borderColor"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isTouching)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        else
            Color.Transparent,
        animationSpec = tween(150),
        label = "bgColor"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .background(backgroundColor)
            .border(
                width = if (isTouching) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .pointerInput(Unit) {
                awaitEachGesture {
                    val firstDown = awaitFirstDown()
                    firstDown.consume()

                    // Start touch feedback
                    isTouching = true
                    touchPosition = firstDown.position
                    fingerCount = 1

                    var lastPosition = firstDown.position
                    var isTwoFinger = false
                    var hasMoved = false
                    var lastTwoFingerCenter = Offset.Zero
                    val moveThreshold = 10f

                    while (true) {
                        val event = awaitPointerEvent()
                        val activePointers = event.changes.filter { it.pressed }

                        if (activePointers.isEmpty()) {
                            // End touch feedback
                            isTouching = false
                            fingerCount = 0

                            if (!hasMoved && !isTwoFinger) {
                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastTapTime < doubleTapTimeout) {
                                    onSendMouse(0, 0, 0, false, true)
                                    onSendMouse(0, 0, 0, false, false)
                                    lastTapTime = 0L
                                } else {
                                    lastTapTime = currentTime
                                    scope.launch {
                                        delay(doubleTapTimeout)
                                        if (lastTapTime == currentTime) {
                                            onSendMouse(0, 0, 0, true, false)
                                            onSendMouse(0, 0, 0, false, false)
                                        }
                                    }
                                }
                            }
                            event.changes.forEach { it.consume() }
                            break
                        }

                        // Update finger count for visual feedback
                        fingerCount = activePointers.size

                        if (activePointers.size >= 2) {
                            isTwoFinger = true
                            val center = Offset(
                                activePointers.map { it.position.x }.average().toFloat(),
                                activePointers.map { it.position.y }.average().toFloat()
                            )
                            touchPosition = center

                            if (lastTwoFingerCenter != Offset.Zero) {
                                val deltaY = center.y - lastTwoFingerCenter.y
                                val scrollAmount = (-deltaY * scrollSensitivity).toInt()
                                if (scrollAmount != 0) {
                                    onSendMouse(0, 0, scrollAmount, leftBtnPressed, rightBtnPressed)
                                }
                            }
                            lastTwoFingerCenter = center
                        } else if (activePointers.size == 1 && !isTwoFinger) {
                            val currentPosition = activePointers.first().position
                            touchPosition = currentPosition
                            val delta = currentPosition - lastPosition

                            if (delta.getDistance() > moveThreshold) {
                                hasMoved = true
                            }

                            if (hasMoved) {
                                onSendMouse(
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
            },
        contentAlignment = Alignment.Center
    ) {
        // Hint text (fades when touching)
        val textAlpha by animateFloatAsState(
            targetValue = if (isTouching) 0.3f else 1f,
            animationSpec = tween(150),
            label = "textAlpha"
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.alpha(textAlpha)
        ) {
            Text(
                text = if (isTouching && fingerCount >= 2) "Scrolling" else "Move cursor",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Tap to click • Double-tap for right-click • Two fingers to scroll",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun TouchpadAreaPreview() {
    KurzaTheme {
        TouchpadArea(
            modifier = Modifier.height(300.dp),
            leftBtnPressed = false,
            rightBtnPressed = false,
            onSendMouse = { _, _, _, _, _ -> }
        )
    }
}
