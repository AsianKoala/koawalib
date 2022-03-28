package com.asiankoala.koawalib.control.motion

import kotlin.math.absoluteValue
import kotlin.math.pow

data class MotionState(
    val x: Double,
    val v: Double,
    val a: Double
) {
    fun calculate(dt: Double): MotionState {
        return MotionState(x + v * dt + 0.5 * a * dt.pow(2), v + a * dt, a)
    }

    fun integrate(dt: Double): Double {
        return (v * dt + 0.5 * a * dt.pow(2)).absoluteValue
    }
}
