package com.asiankoala.koawalib.control.motion

/**
 * Motion Constraints specify limits of motion of a system
 * @see MotionProfile
 * @param vMax max velocity
 * @param aMax max acceleration
 * @param dMax max deceleration
 * @param minCruiseTime minimum time spent at constant velocity in a trapezoidal motion profile
 */
data class MotionConstraints(
    var vMax: Double,
    var aMax: Double,
    var dMax: Double,
)
