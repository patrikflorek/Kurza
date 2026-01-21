package io.github.patrikflorek.kurza.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.patrikflorek.kurza.ui.theme.KurzaTheme
import io.github.patrikflorek.kurza.ui.theme.SurfaceElevated

@Composable
fun MouseButtonsRow(
    leftBtnPressed: Boolean,
    rightBtnPressed: Boolean,
    onLeftBtnPressedChange: (Boolean) -> Unit,
    onRightBtnPressedChange: (Boolean) -> Unit,
    onMouseEvent: (MouseEvent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left Button
        MouseButton(
            modifier = Modifier.weight(1f),
            label = "Left",
            isPressed = leftBtnPressed,
            onPressChange = { pressed ->
                onLeftBtnPressedChange(pressed)
                onMouseEvent(MouseEvent.click(leftButton = pressed, rightButton = rightBtnPressed))
            }
        )

        // Right Button
        MouseButton(
            modifier = Modifier.weight(1f),
            label = "Right",
            isPressed = rightBtnPressed,
            onPressChange = { pressed ->
                onRightBtnPressedChange(pressed)
                onMouseEvent(MouseEvent.click(leftButton = leftBtnPressed, rightButton = pressed))
            }
        )
    }
}

@Composable
fun MouseButton(
    modifier: Modifier = Modifier,
    label: String,
    isPressed: Boolean,
    onPressChange: (Boolean) -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) SurfaceElevated else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(100),
        label = "buttonBg"
    )

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    onPressChange(true)
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.none { it.pressed }) {
                            onPressChange(false)
                            break
                        }
                    }
                }
            },
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(
            width = 1.dp,
            color = if (isPressed)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = if (isPressed)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun MouseButtonsRowPreview() {
    KurzaTheme {
        MouseButtonsRow(
            leftBtnPressed = false,
            rightBtnPressed = false,
            onLeftBtnPressedChange = {},
            onRightBtnPressedChange = {},
            onMouseEvent = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun MouseButtonsRowPressedPreview() {
    KurzaTheme {
        MouseButtonsRow(
            leftBtnPressed = true,
            rightBtnPressed = false,
            onLeftBtnPressedChange = {},
            onRightBtnPressedChange = {},
            onMouseEvent = {}
        )
    }
}
