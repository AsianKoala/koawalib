package com.asiankoala.koawalib.path

import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.radians

object Testing {
    @JvmStatic
    fun main(args: Array<String>) {
        val t = QuinticSplinePath(
            Pose(),
            Pose(20.0, 20.0, 90.0.radians),
        )

        println(t[0.0, 1].vec)
    }
}
