package com.asiankoala.koawalib.gamepad

import com.asiankoala.koawalib.gamepad.functionality.Button

abstract class ButtonBase : Button {
    private var _isPressed = false
    private var _isToggled = false
    private var _recentAction = false
    private var _pastState = false

    override val isPressed: Boolean
        get() = _isPressed

    override val isToggled
        get() = _isToggled

    override val recentAction: Boolean
        get() = _recentAction

    override val pastState: Boolean
        get() = _pastState

    override fun periodic() {
        val currentState = invokeBoolean()
        _recentAction = _pastState != currentState
        _pastState = currentState
        _isPressed = currentState

        // TODO CHECK IF WORKS
        if (recentAction && pastState) {
            _isToggled = !_isToggled
        }
    }
}
