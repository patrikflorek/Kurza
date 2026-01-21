package link.sciber.kurza.ui.components

import androidx.compose.ui.geometry.Offset

data class GestureState(
    val isTouching: Boolean = false,
    val fingerCount: Int = 0,
    val touchPosition: Offset = Offset.Zero
)

sealed class MouseAction {
    data class Move(val dx: Int, val dy: Int, val leftBtn: Boolean, val rightBtn: Boolean) : MouseAction()
    data class Scroll(val amount: Int, val leftBtn: Boolean, val rightBtn: Boolean) : MouseAction()
    data class Click(val leftBtn: Boolean, val rightBtn: Boolean) : MouseAction()
}

class TouchpadGestureHandler(
    private val sensitivity: Float = DEFAULT_SENSITIVITY,
    private val scrollSensitivity: Float = DEFAULT_SCROLL_SENSITIVITY,
    private val doubleTapTimeout: Long = 300L,
    private val moveThreshold: Float = 10f,
    private val timeProvider: () -> Long = { System.currentTimeMillis() }
) {
    private var lastTapTime: Long = 0L
    private var lastPosition: Offset = Offset.Zero
    private var isTwoFinger: Boolean = false
    private var hasMoved: Boolean = false
    private var lastTwoFingerCenter: Offset = Offset.Zero
    
    private var _gestureState = GestureState()
    val gestureState: GestureState get() = _gestureState

    fun onGestureStart(position: Offset) {
        _gestureState = GestureState(
            isTouching = true,
            fingerCount = 1,
            touchPosition = position
        )
        lastPosition = position
        isTwoFinger = false
        hasMoved = false
        lastTwoFingerCenter = Offset.Zero
    }

    fun onGestureEnd(leftBtnPressed: Boolean, rightBtnPressed: Boolean): TapResult {
        _gestureState = GestureState(isTouching = false, fingerCount = 0)
        
        if (!hasMoved && !isTwoFinger) {
            val currentTime = timeProvider()
            return if (currentTime - lastTapTime < doubleTapTimeout) {
                lastTapTime = 0L
                TapResult.DoubleTap
            } else {
                lastTapTime = currentTime
                TapResult.SingleTapPending(currentTime)
            }
        }
        return TapResult.None
    }

    fun shouldExecuteSingleTap(pendingTapTime: Long): Boolean {
        return lastTapTime == pendingTapTime
    }

    fun onSingleFingerMove(
        currentPosition: Offset,
        leftBtnPressed: Boolean,
        rightBtnPressed: Boolean
    ): MouseAction? {
        if (isTwoFinger) {
            // Transition from scroll to move: reset and anchor to current position
            isTwoFinger = false
            lastPosition = currentPosition
            lastTwoFingerCenter = Offset.Zero
            return null  // Skip this frame to avoid cursor jump
        }
        
        _gestureState = _gestureState.copy(
            fingerCount = 1,
            touchPosition = currentPosition
        )
        
        val delta = currentPosition - lastPosition
        
        if (delta.getDistance() > moveThreshold) {
            hasMoved = true
        }
        
        return if (hasMoved) {
            lastPosition = currentPosition
            MouseAction.Move(
                dx = (delta.x * sensitivity).toInt(),
                dy = (delta.y * sensitivity).toInt(),
                leftBtn = leftBtnPressed,
                rightBtn = rightBtnPressed
            )
        } else {
            null
        }
    }

    fun onTwoFingerMove(
        positions: List<Offset>,
        leftBtnPressed: Boolean,
        rightBtnPressed: Boolean
    ): MouseAction? {
        isTwoFinger = true
        
        val center = Offset(
            positions.map { it.x }.average().toFloat(),
            positions.map { it.y }.average().toFloat()
        )
        
        _gestureState = _gestureState.copy(
            fingerCount = positions.size,
            touchPosition = center
        )
        
        val action = if (lastTwoFingerCenter != Offset.Zero) {
            val deltaY = center.y - lastTwoFingerCenter.y
            val scrollAmount = (-deltaY * scrollSensitivity).toInt()
            if (scrollAmount != 0) {
                MouseAction.Scroll(scrollAmount, leftBtnPressed, rightBtnPressed)
            } else {
                null
            }
        } else {
            null
        }
        
        lastTwoFingerCenter = center
        return action
    }

    fun reset() {
        lastTapTime = 0L
        lastPosition = Offset.Zero
        isTwoFinger = false
        hasMoved = false
        lastTwoFingerCenter = Offset.Zero
        _gestureState = GestureState()
    }
}

sealed class TapResult {
    object None : TapResult()
    object DoubleTap : TapResult()
    data class SingleTapPending(val tapTime: Long) : TapResult()
}
