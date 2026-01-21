package io.github.patrikflorek.kurza.ui.components

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import io.github.patrikflorek.kurza.ui.theme.KurzaTheme

const val DEFAULT_SENSITIVITY = 1.5f
const val DEFAULT_SCROLL_SENSITIVITY = 0.3f

@Composable
fun TouchpadArea(
    modifier: Modifier = Modifier,
    leftBtnPressed: Boolean,
    rightBtnPressed: Boolean,
    sensitivity: Float = DEFAULT_SENSITIVITY,
    scrollSensitivity: Float = DEFAULT_SCROLL_SENSITIVITY,
    onMouseEvent: (MouseEvent) -> Unit
) {
    val scope = rememberCoroutineScope()
    val gestureHandler = remember {
        TouchpadGestureHandler(
            sensitivity = sensitivity,
            scrollSensitivity = scrollSensitivity
        )
    }

    // Visual feedback state derived from gesture handler
    var gestureState by remember { mutableStateOf(GestureState()) }

    val borderColor by animateColorAsState(
        targetValue = if (gestureState.isTouching)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        else
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        animationSpec = tween(150),
        label = "borderColor"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (gestureState.isTouching)
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
                width = if (gestureState.isTouching) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .pointerInput(leftBtnPressed, rightBtnPressed) {
                awaitEachGesture {
                    val firstDown = awaitFirstDown()
                    firstDown.consume()

                    gestureHandler.onGestureStart(firstDown.position)
                    gestureState = gestureHandler.gestureState

                    while (true) {
                        val event = awaitPointerEvent()
                        val activePointers = event.changes.filter { it.pressed }

                        if (activePointers.isEmpty()) {
                            when (val result = gestureHandler.onGestureEnd(leftBtnPressed, rightBtnPressed)) {
                                is TapResult.DoubleTap -> {
                                    onMouseEvent(MouseEvent.rightClick())
                                    onMouseEvent(MouseEvent.release())
                                }
                                is TapResult.SingleTapPending -> {
                                    val tapTime = result.tapTime
                                    scope.launch {
                                        delay(300L)
                                        if (gestureHandler.shouldExecuteSingleTap(tapTime)) {
                                            onMouseEvent(MouseEvent.leftClick())
                                            onMouseEvent(MouseEvent.release())
                                        }
                                    }
                                }
                                TapResult.None -> {}
                            }
                            gestureState = gestureHandler.gestureState
                            event.changes.forEach { it.consume() }
                            break
                        }

                        val action = if (activePointers.size >= 2) {
                            gestureHandler.onTwoFingerMove(
                                activePointers.map { it.position },
                                leftBtnPressed,
                                rightBtnPressed
                            )
                        } else {
                            gestureHandler.onSingleFingerMove(
                                activePointers.first().position,
                                leftBtnPressed,
                                rightBtnPressed
                            )
                        }

                        gestureState = gestureHandler.gestureState

                        when (action) {
                            is MouseAction.Move -> onMouseEvent(MouseEvent.move(action.dx, action.dy, action.leftBtn, action.rightBtn))
                            is MouseAction.Scroll -> onMouseEvent(MouseEvent.scroll(action.amount, action.leftBtn, action.rightBtn))
                            is MouseAction.Click -> onMouseEvent(MouseEvent.click(action.leftBtn, action.rightBtn))
                            null -> {}
                        }

                        event.changes.forEach { it.consume() }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Hint text (fades when touching)
        val textAlpha by animateFloatAsState(
            targetValue = if (gestureState.isTouching) 0.3f else 1f,
            animationSpec = tween(150),
            label = "textAlpha"
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.alpha(textAlpha)
        ) {
            Text(
                text = if (gestureState.isTouching && gestureState.fingerCount >= 2) "Scrolling" else "Move cursor",
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
            onMouseEvent = {}
        )
    }
}
