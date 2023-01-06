package com.asiankoala.koawalib.gamepad

import com.asiankoala.koawalib.gamepad.functionality.ButtonProcessing

/**
 * Button functionality
 * @property isPressed is button pressed
 * @property isJustPressed was button just pressed (state changed this loop)
 * @property isJustReleased was button just released (state changed this loop)
 * @property isReleased is button not pressed
 */
class KButton(
    private val buttonState: () -> Boolean
) : ButtonProcessing() {
    override fun invoke() = buttonState.invoke()
}
