package com.asiankoala.koawalib.path

import com.asiankoala.koawalib.math.*
import com.asiankoala.koawalib.math.Point

data class Waypoint(
    val x: Double,
    val y: Double,
    val followDistance: Double,
    val headingLockAngle: Double? = null,
    val maxMoveSpeed: Double = 1.0,
    val maxTurnSpeed: Double = 1.0,
    val deccelAngle: Double = 60.0.radians,
    val stop: Boolean = true,
    val minAllowedHeadingError: Double = 60.0.radians,
    val lowestSlowDownFromHeadingError: Double = 0.4,
//    val command: Command? = null
) {
    val point = Point(x, y)

    val copy: Waypoint
        get() = Waypoint(
            x, y, followDistance, headingLockAngle, maxMoveSpeed, maxTurnSpeed, deccelAngle,
            stop, minAllowedHeadingError, lowestSlowDownFromHeadingError/*, command*/
        )
}
