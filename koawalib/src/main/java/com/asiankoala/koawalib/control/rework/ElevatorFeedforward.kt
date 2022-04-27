package com.asiankoala.koawalib.control.rework

import kotlin.math.sign

@Suppress("unused")
class ElevatorFeedforward(
    private val kG: Double,
    private val kS: Double,
    private val kV: Double,
    private val kA: Double = 0.0
) {
    fun calculate(v: Double, a: Double = 0.0): Double {
        return kG + kS * v.sign + kV * v + kA * a
    }
}