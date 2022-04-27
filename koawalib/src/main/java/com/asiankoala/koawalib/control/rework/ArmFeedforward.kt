package com.asiankoala.koawalib.control.rework

import com.asiankoala.koawalib.math.cos
import kotlin.math.sign

@Suppress("unused")
class ArmFeedforward(
    private val kCos: Double,
    private val kS: Double,
    private val kV: Double,
    private val kA: Double = 0.0,
) {
    fun calculate(theta: Double, v: Double, a: Double = 0.0): Double {
        return kCos * theta.cos + kS * v.sign + kV * v + kA * a
    }

}