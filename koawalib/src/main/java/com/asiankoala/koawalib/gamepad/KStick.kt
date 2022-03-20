package com.asiankoala.koawalib.gamepad

import com.asiankoala.koawalib.gamepad.functionality.Stick

class KStick(
    private val stickXAxis: KAxis,
    private val stickYAxis: KAxis,
    private val stickButton: KButton
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

    val xInverted
        get() = KStick(stickXAxis.inverted, stickYAxis, stickButton)

    val yInverted
        get() = KStick(stickXAxis, stickYAxis.inverted, stickButton)
}
