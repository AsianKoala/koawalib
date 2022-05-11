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
    var x: Double = 0.0,
    var v: Double = 0.0,
    var a: Double = 0.0
) {
    /**
     * Calculate a new motion state by integrating constant acceleration
     * @param t time
     */
    operator fun get(t: Double) = MotionState(x + v * t + 0.5 * a * t.pow(2), v + a * t, a)
}
