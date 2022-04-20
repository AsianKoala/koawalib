package com.asiankoala.koawalib.gamepad.functionality

import com.asiankoala.koawalib.util.KBoolean
import com.asiankoala.koawalib.util.Periodic

/**
 * Button functionality
 * @property isPressed is button pressed
 * @property isJustPressed was button just pressed (state changed this loop)
 * @property isJustReleased was button just released (state changed this loop)
 * @property isReleased is button not pressed
 */
abstract class Button : KBoolean, Periodic {
    var isPressed: Boolean = false
        private set
    private var hasChanged: Boolean = false
    private var lastState: Boolean = false

    val isJustPressed: Boolean
        get() = isPressed && hasChanged

    val isJustReleased: Boolean
        get() = !isPressed && hasChanged

    val isReleased: Boolean
        get() = !isPressed

    override fun periodic() {
        val currentState = invokeBoolean()
        hasChanged = lastState != currentState
        lastState = currentState
        isPressed = currentState
    }
}
