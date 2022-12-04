package com.asiankoala.koawalib.gamepad

import com.asiankoala.koawalib.control.filter.SlewRateLimiter
import com.asiankoala.koawalib.gamepad.functionality.Stick
import com.asiankoala.koawalib.util.internal.cond
import kotlin.math.absoluteValue

class KStick(
    private val stickXAxis: KAxis,
    private val stickYAxis: KAxis,
    private val stickButton: KButton,
    private var xRateLimiter: SlewRateLimiter? = null,
    private var yRateLimiter: SlewRateLimiter? = null,
    private var deadzone: Double? = null
) : Stick {
    override val xAxis: Double
        get() {
            val x = stickXAxis.invoke()
            return x
                .cond(deadzone != null && x in -deadzone!!..deadzone!!) { 0.0 }
                .cond(xRateLimiter != null) { xRateLimiter!!.calculate(it) }
        }


    override val yAxis: Double
        get() {
            val y = stickYAxis.invoke()
            return y
                .cond(deadzone != null && y in -deadzone!!..deadzone!!) { 0.0 }
                .cond(yRateLimiter != null) { yRateLimiter!!.calculate(it) }
        }

    val xInverted
        get() = KStick(
            stickXAxis.inverted(),
            stickYAxis,
            stickButton,
            xRateLimiter,
            yRateLimiter,
            deadzone
        )

    val yInverted
        get() = KStick(
            stickXAxis,
            stickYAxis.inverted(),
            stickButton,
            xRateLimiter,
            yRateLimiter,
            deadzone
        )

    override fun periodic() {
        stickButton.periodic()
    }

    fun setXRateLimiter(rateLimiter: SlewRateLimiter) {
        xRateLimiter = rateLimiter
    }

    fun setYRateLimiter(rateLimiter: SlewRateLimiter) {
        yRateLimiter = rateLimiter
    }

    fun setDeadzone(threshold: Double) {
        deadzone = threshold
    }
}
