package link.sciber.kurza.ui.components

data class MouseEvent(
    val dx: Int = 0,
    val dy: Int = 0,
    val scroll: Int = 0,
    val leftButton: Boolean = false,
    val rightButton: Boolean = false
) {
    companion object {
        fun move(dx: Int, dy: Int, leftButton: Boolean = false, rightButton: Boolean = false) =
            MouseEvent(dx = dx, dy = dy, leftButton = leftButton, rightButton = rightButton)

        fun scroll(amount: Int, leftButton: Boolean = false, rightButton: Boolean = false) =
            MouseEvent(scroll = amount, leftButton = leftButton, rightButton = rightButton)

        fun click(leftButton: Boolean = false, rightButton: Boolean = false) =
            MouseEvent(leftButton = leftButton, rightButton = rightButton)

        fun leftClick() = MouseEvent(leftButton = true)
        fun rightClick() = MouseEvent(rightButton = true)
        fun release() = MouseEvent()
    }
}
