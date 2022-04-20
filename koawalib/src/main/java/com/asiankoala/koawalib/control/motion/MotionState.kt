package com.asiankoala.koawalib.control.motion

import kotlin.math.absoluteValue
import kotlin.math.pow

/**
 * Represents a motion at a snapshot in time of a system
 * @see MotionProfile
 * @param x position of system
 * @param v velocity of system
 * @param a acceleration of system
 */
data class MotionState(
    val x: Double,
    val v: Double,
    val a: Double
) {
    /**
     * Calculate a new motion state by integrating constant acceleration
     * @param dt time
     */
    fun calculate(dt: Double): MotionState {
        return MotionState(x + v * dt + 0.5 * a * dt.pow(2), v + a * dt, a)
    }

    /**
     * Take a definite integral of the motion state to calculate distance
     */
    fun integrate(dt: Double): Double {
        return (v * dt + 0.5 * a * dt.pow(2)).absoluteValue
    }
}
