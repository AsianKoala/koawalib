package com.asiankoala.koawalib

import com.asiankoala.koawalib.control.motion.MotionConstraints
import com.asiankoala.koawalib.control.motion.MotionProfile
import com.asiankoala.koawalib.control.motion.MotionState
import com.asiankoala.koawalib.math.d
import jetbrains.letsPlot.elementRect
import jetbrains.letsPlot.export.ggsave
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.ggplot
import jetbrains.letsPlot.theme

object MotionProfileTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val start = MotionState()
        val end = MotionState(200.0)
        val constraints = MotionConstraints(30.0, 30.0)
        val profile = MotionProfile(start, end, constraints)
        val duration = profile.duration
        println(duration)
        val resolution = 200
        val vs = DoubleArray(resolution)
        val ts = DoubleArray(resolution)
        for(i in 0 until resolution) {
            val t = (i.d / resolution.d) * duration
            ts[i] = t
            vs[i] = profile[t].v
        }

        val data = mapOf<String, Any>("t" to ts, "v" to vs)
        val fig = ggplot(data) +
                geomPoint(color = "red", size = 1.0) { x = "t"; y = "v" } +
                theme(plotBackground = elementRect(fill = "black"))
        ggsave(fig, "plot.png")
    }
}