package com.asiankoala.koawalib.gamepad.functionality

import com.asiankoala.koawalib.util.Periodic
import com.asiankoala.koawalib.util.internal.KBoolean

abstract class ButtonProcessing : ButtonFunc, KBoolean, Periodic {
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
        val currentState = invokeBoolean()
        _hasChanged = _lastState != currentState
        _lastState = currentState
        _isPressed = currentState
        if (isJustPressed) _isToggled = !_isToggled
    }
}