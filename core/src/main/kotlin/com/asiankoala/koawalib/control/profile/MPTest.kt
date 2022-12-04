package com.asiankoala.koawalib.control.profile

import com.asiankoala.koawalib.path.gvf.Constraints
import com.asiankoala.koawalib.path.gvf.DispState
import com.asiankoala.koawalib.path.gvf.OnlineProfile
import jetbrains.letsPlot.elementRect
import jetbrains.letsPlot.export.ggsave
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.ggplot
import jetbrains.letsPlot.theme


internal object MPTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val start = MotionState(-69.0)
        val end = MotionState(0.0)
        val constraints = MotionConstraints(180.0, 180.0)
        val profile = MotionProfile.generateTrapezoidal(start, end, constraints)

        val st = ArrayList<Double>()
        val sx = ArrayList<Double>()
        val sv = ArrayList<Double>()
        val sa = ArrayList<Double>()
        val resolution = 0.01
        var t = 0.0

        while(t < profile.duration) {
            val state = profile[t]
            st.add(t)
            sx.add(state.x)
            sv.add(state.v)
            sa.add(state.a)
            t += resolution
        }

        plot(st, sx, sv, sa)

//        val start = DispState(0.0)
//        val end = DispState(-69.0)
//        val constraints = Constraints(180.0, 180.0)
//        val profile = OnlineProfile(start, end, constraints)
//        val sx = ArrayList<Double>()
//        val sv = ArrayList<Double>()
//        val resolution = 1.0
//        var x = 0.0
//
//        while(x > end.x) {
//            val state = profile[x]
//            sx.add(x)
//            sv.add(state.v)
//            x -= resolution
//        }
//
//        println(sx[60])
//
//        plotDisp(sx, sv)
    }

    private fun plot(
        st: ArrayList<Double>,
        sx: ArrayList<Double>,
        sv: ArrayList<Double>,
        sa: ArrayList<Double>
    ) {
        val data = mapOf<String, Any>("t" to st, "x" to sx, "v" to sv, "a" to sa)
        val fig = ggplot(data) +
                geomPoint(color = "blue", size = 1.0) { x = "t"; y = "x" } +
                geomPoint(color = "green", size = 1.0) { x = "t"; y = "v" } +
                geomPoint(color = "red", size = 1.0) { x = "t"; y = "a" } +
                theme(plotBackground = elementRect(fill = "black"))
        ggsave(fig, "plot.png")
    }

    private fun plotDisp(
        sx: ArrayList<Double>,
        sv: ArrayList<Double>,
    ) {
        val data = mapOf<String, Any>("x" to sx, "v" to sv)
        val fig = ggplot(data) +
                geomPoint(color = "blue", size = 1.0) { x = "x"; y = "v" } +
                theme(plotBackground = elementRect(fill = "black"))
        ggsave(fig, "plot.png")
    }
}