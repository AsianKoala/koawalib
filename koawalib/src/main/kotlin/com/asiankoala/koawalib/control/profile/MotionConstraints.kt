package com.asiankoala.koawalib.control.profile

/**
 * Motion Constraints specify limits of motion of a system
 * @see MotionProfile
 * @param maxV max velocity
 * @param accel max acceleration
 * @param deccel max deceleration
 */
data class MotionConstraints(
    val maxV: Double,
    val accel: Double,
    private val rawDeccel: Double = accel
) {
    val deccel: Double

    init {
        deccel = -rawDeccel
    }
}
