package com.asiankoala.koawalib.control.filter

import com.asiankoala.koawalib.math.clamp
import com.asiankoala.koawalib.util.Clock

class SlewRateLimiter(
    private val r: Double,
) {
    private var ukm1 = 0.0
    private var tkm1 = Clock.seconds

    fun calculate(input: Double): Double {
        val dt = Clock.seconds - tkm1
        ukm1 += clamp(input - ukm1, -r * dt, r * dt)
        tkm1 = Clock.seconds
        return ukm1
    }

    fun reset(u: Double) {
        ukm1 = u
        tkm1 = Clock.seconds
    }
}
