package com.asiankoala.koawalib.gamepad

import com.asiankoala.koawalib.gamepad.functionality.Stick
import com.asiankoala.koawalib.util.KDouble

class KStick(
    private val stickXAxis: KDouble,
    private val stickYAxis: KDouble,
    private val stickButton: KButton,
) : Stick {
    override fun periodic() {
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
