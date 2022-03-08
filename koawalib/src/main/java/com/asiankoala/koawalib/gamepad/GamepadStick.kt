package com.asiankoala.koawalib.gamepad

import com.asiankoala.koawalib.gamepad.functionality.Stick

class GamepadStick(
    private val stickXAxis: GamepadAxis,
    private val stickYAxis: GamepadAxis,
    private val stickButton: GamepadButton
) : Stick {
    override fun periodic() {
        stickXAxis.periodic()
        stickYAxis.periodic()
        stickButton.periodic()
    }

    override val xAxis
        get() = stickXAxis.invokeDouble()

    override val yAxis
        get() = stickYAxis.invokeDouble()
}
