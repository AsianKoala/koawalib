package com.asiankoala.koawalib.gamepad

import com.asiankoala.koawalib.util.Periodic

class GamepadDpad(
    private val up: GamepadButton,
    private val down: GamepadButton,
    private val left: GamepadButton,
    private val right: GamepadButton
) : Periodic {

    override fun periodic() {
        up.periodic()
        down.periodic()
        left.periodic()
        right.periodic()
    }
}
