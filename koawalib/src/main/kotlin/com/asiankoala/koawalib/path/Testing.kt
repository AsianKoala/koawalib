package com.asiankoala.koawalib.path

import com.asiankoala.koawalib.math.Pose

object Testing {
    @JvmStatic
    fun main(args: Array<String>) {
        val t = Path(Pose(), Pose(16.0, 16.0))
        print(t[t.length / 2.0])
    }
}