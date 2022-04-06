package com.asiankoala.koawalib.gamepad.functionality

// todo combine this with CommandButton
abstract class ButtonFunc : Button {
    private var _isPressed = false
    private var _isToggled = false
    private var _hasChanged = false
    private var _lastState = false

    override val isPressed: Boolean
        get() = _isPressed

    override val isToggled: Boolean
        get() = _isToggled

    override val hasChanged: Boolean
        get() = _hasChanged

    override val lastState: Boolean
        get() = _lastState

    override fun periodic() {
        val currentState = invokeBoolean()
        _hasChanged = _lastState != currentState
        _lastState = currentState
        _isPressed = currentState

        if (isJustPressed) {
            _isToggled = !_isToggled
        }
    }
}
