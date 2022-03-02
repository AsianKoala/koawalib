package com.asiankoala.koawalib.gamepad

import com.asiankoala.koawalib.gamepad.functionality.Input

class GamepadButton(private val button: () -> Boolean) : ButtonBase(), Input<GamepadButton> {
    override fun instance(): GamepadButton {
        return this
    }

    override fun invokeBoolean(): Boolean {
        return button.invoke()
    }
}
