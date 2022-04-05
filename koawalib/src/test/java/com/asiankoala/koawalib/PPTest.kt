package com.asiankoala.koawalib

import com.asiankoala.koawalib.math.*
import com.asiankoala.koawalib.path.Path
import com.asiankoala.koawalib.path.PurePursuitController
import com.asiankoala.koawalib.path.Waypoint
import kotlin.math.abs

object PPTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val myPose = Pose(17.64, 17.61, 2.83.radians)
        val waypoints = listOf(
            Waypoint(0.0, 0.0, 0.0),
            Waypoint(0.0, 18.0, 8.0),
            Waypoint(18.0, 18.0, 8.0)
        )

        val clipped = PurePursuitController.clipToPath(waypoints, myPose)
        println(clipped)
    }
}
