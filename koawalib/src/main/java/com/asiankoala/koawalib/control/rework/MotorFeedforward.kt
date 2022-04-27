package com.asiankoala.koawalib.control.rework

import kotlin.math.sign

@Suppress("unused")
class MotorFeedforward(
    private val kS: Double,
    private val kV: Double,
    private val kA: Double = 0.0
) : Feedforward {
    fun calculate(v: Double, a: Double = 0.0): Double {
        return kS * v.sign + kV * v + kA * a
    }
}