package com.asiankoala.koawalib.control.profile

/**
 * Motion Constraints specify limits of motion of a system
 * @see MotionProfile
 * @param cruiseVel max velocity
 * @param accel max acceleration
 * @param deccel max deceleration
 */
data class MotionConstraints(
    val cruiseVel: Double,
    val accel: Double,
    val deccel: Double = accel,
)
