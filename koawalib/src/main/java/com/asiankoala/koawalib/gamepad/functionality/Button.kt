package com.asiankoala.koawalib.gamepad.functionality

import com.asiankoala.koawalib.util.KBoolean
import com.asiankoala.koawalib.util.Periodic

abstract class Button : KBoolean, Periodic {
    var isPressed: Boolean = false
        private set
    var isToggled: Boolean = false
        private set
    private var hasChanged: Boolean = false
    private var lastState: Boolean = false

    val isJustPressed: Boolean
        get() = isPressed && hasChanged

    val isJustReleased: Boolean
        get() = !isPressed && hasChanged

    val isReleased: Boolean
        get() = !isPressed

    val isJustToggled: Boolean
        get() = isToggled && hasChanged && isPressed

    val isJustUntoggled: Boolean
        get() = !isToggled && hasChanged && isPressed

    val isUntoggled: Boolean
        get() = !isToggled

    override fun periodic() {
        val currentState = invokeBoolean()
        hasChanged = lastState != currentState
        lastState = currentState
        isPressed = currentState

        if (isJustPressed) {
            isToggled = !isToggled
        }
    }
}
