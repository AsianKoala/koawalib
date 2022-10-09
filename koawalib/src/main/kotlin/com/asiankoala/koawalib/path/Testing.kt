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

        println(t[t.length * 0.99])
    }
}
