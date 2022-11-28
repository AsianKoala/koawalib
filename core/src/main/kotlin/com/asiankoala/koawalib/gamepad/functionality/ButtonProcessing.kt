package com.asiankoala.koawalib.gamepad.functionality

import com.asiankoala.koawalib.util.Periodic

abstract class ButtonProcessing : ButtonFunc, () -> Boolean, Periodic {
    private var _isPressed = false
    private var _isToggled = false
    private var _hasChanged = false
    private var _lastState = false

    override val isPressed: Boolean
        get() = _isPressed

    override val isToggled: Boolean
        get() = _isToggled

    override val isJustPressed: Boolean
        get() = isPressed && _hasChanged

    override val isJustReleased: Boolean
        get() = !isPressed && _hasChanged

    override val isReleased: Boolean
        get() = !isPressed

    override fun periodic() {
        val currentState = invoke()
        _hasChanged = _lastState != currentState
        _lastState = currentState
        _isPressed = currentState
        if (isJustPressed) _isToggled = !_isToggled
    }
}
