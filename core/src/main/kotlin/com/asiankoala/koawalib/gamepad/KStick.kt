package com.asiankoala.koawalib.gamepad

import com.asiankoala.koawalib.control.filter.SlewRateLimiter
import com.asiankoala.koawalib.gamepad.functionality.Stick

class KStick(
    private val stickXAxis: KAxis,
    private val stickYAxis: KAxis,
    private val stickButton: KButton,
    private var xRateLimiter: SlewRateLimiter? = null,
    private var yRateLimiter: SlewRateLimiter? = null,
) : Stick {
    private var isXRateLimited = xRateLimiter != null
    private var isYRateLimited = yRateLimiter != null

    override val xAxis
        get() = if (isXRateLimited) {
            xRateLimiter!!.calculate(stickXAxis.invoke())
        } else stickXAxis.invoke()

    override val yAxis
        get() = if (isYRateLimited) {
            yRateLimiter!!.calculate(stickYAxis.invoke())
        } else stickYAxis.invoke()

    val xInverted
        get() = KStick(
            stickXAxis.inverted(),
            stickYAxis,
            stickButton,
            xRateLimiter,
            yRateLimiter
        )

    val yInverted
        get() = KStick(
            stickXAxis,
            stickYAxis.inverted(),
            stickButton,
            xRateLimiter,
            yRateLimiter
        )

    override fun periodic() {
        stickButton.periodic()
    }

    fun setXRateLimiter(rateLimiter: SlewRateLimiter) {
        xRateLimiter = rateLimiter
        isXRateLimited = true
    }

    fun setYRateLimiter(rateLimiter: SlewRateLimiter) {
        yRateLimiter = rateLimiter
        isYRateLimited = true
    }
}
