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
        val constraints = MotionConstraints(60.0, 45.0, 15.0)
        val profile = MotionProfile(start, end, constraints)
        val duration = profile.duration
        println(duration)
        val resolution = 200
        val ts = DoubleArray(resolution)
        val xs = DoubleArray(resolution)
        val vs = DoubleArray(resolution)
        val `as` = DoubleArray(resolution)
        for(i in 0 until resolution) {
            val t = (i.d / resolution.d) * duration
            ts[i] = t
            val state = profile[t]
            vs[i] = state.v
            xs[i] = state.x
            `as`[i] = state.a
        }

        val data = mapOf<String, Any>("t" to ts, "x" to xs, "v" to vs, "a" to `as`)
        val fig = ggplot(data) +
                geomPoint(color = "blue", size = 1.0) { x = "t"; y = "x"} +
                geomPoint(color = "red", size = 1.0) { x = "t"; y = "v" } +
                geomPoint(color = "green", size= 1.0) { x = "t"; y = "a"} +
                theme(plotBackground = elementRect(fill = "gray"))
        ggsave(fig, "plot.png")
    }
}