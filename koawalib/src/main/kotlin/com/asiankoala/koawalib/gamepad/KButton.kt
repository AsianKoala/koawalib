package com.asiankoala.koawalib.gamepad

import com.asiankoala.koawalib.gamepad.functionality.ButtonFunc
import com.asiankoala.koawalib.util.Periodic
import com.asiankoala.koawalib.util.internal.KBoolean

/**
 * Button functionality
 * @property isPressed is button pressed
 * @property isJustPressed was button just pressed (state changed this loop)
 * @property isJustReleased was button just released (state changed this loop)
 * @property isReleased is button not pressed
 */
open class KButton(
    private val buttonState: () -> Boolean
) : ButtonFunc, KBoolean, Periodic {
    private var _isPressed = false
    private var _isToggled = false
    private var _hasChanged: Boolean = false
    private var _lastState: Boolean = false

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
        if(_hasChanged) _isToggled = !_isToggled
    }

    override fun invokeBoolean(): Boolean {
        return buttonState.invoke()
    }
}
