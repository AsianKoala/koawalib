package com.asiankoala.koawalib.control.motion

/**
 * Motion Constraints specify limits of motion of a system
 * @see MotionProfile
 * @param cruiseVel max velocity
 * @param accel max acceleration
 * @param deccel max deceleration
 */
data class MotionConstraints(
    var cruiseVel: Double,
    var accel: Double,
    var deccel: Double = -accel,
)
