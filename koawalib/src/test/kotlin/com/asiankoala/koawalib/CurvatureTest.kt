//package com.asiankoala.koawalib
//
//import com.acmerobotics.roadrunner.geometry.Vector2d
//import com.acmerobotics.roadrunner.path.PathBuilder
//import com.asiankoala.koawalib.math.*
//import jetbrains.letsPlot.elementRect
//import jetbrains.letsPlot.export.ggsave
//import jetbrains.letsPlot.geom.geomPoint
//import jetbrains.letsPlot.ggplot
//import jetbrains.letsPlot.theme
//
///**
// * Motivation:
// * The way GVF works, heading is only controlled by the angle of the GVF result
// * If the robot behaves perfectly, then this would work fine
// * However, due to acceleration not being infinite, the robot will most likely overshoot around high
// * curvature / second derivative parts of the spline
// * This test is an attempt to find a way to characterize those parts of the spline without a motion profile]
// */
//object CurvatureTest {
//    @JvmStatic
//    fun main(args: Array<String>) {
//        val startPose = Pose(heading = 90.0.radians)
//        val path = PathBuilder(startPose.toPose2d())
//            .splineTo(Vector2d(24.0, 24.0), endTangent = 90.0.radians)
//            .build()
//
//        val length = path.length()
//        val resolution = 200
//        val xs = DoubleArray(200)
//        val ys = DoubleArray(200)
//        val curvatures = ArrayList<Pair<Double, Double>>()
//        for(i in 0 until resolution) {
//            val s = (i.d / resolution.d) * length
//            val p = path[s]
//            xs[i] = p.x
//            ys[i] = p.y
//
//            val gamma = p.vec()
//            val tPrime = path.secondDeriv(s).vec()
//            val radius = 1.0 / tPrime.norm()
//            val c = gamma + tPrime * radius * radius
//            curvatures.add(Pair(c.x, c.y))
//        }
//
//        val min = 0.0
//        val max = 25.0
//        val bounded = curvatures.map {
//            if(it.first !in min..max || it.second !in min..max) {
//                Pair(0.0, 0.0)
//            } else {
//                it
//            }
//        }
//        val cxs = bounded.map { it.first }
//        val cys = bounded.map { it.second }
//
//        val data = mapOf<String, Any>("x" to xs, "y" to ys, "cx" to cxs, "cy" to cys)
//        val fig = ggplot(data) +
//                geomPoint(color = "red", size = 1.0) { x = "x"; y = "y"} +
//                geomPoint(color = "blue", size = 1.0) { x = "cx"; y = "cy" } +
//                theme(plotBackground = elementRect(fill = "gray"))
//        ggsave(fig, "plot.png")
//    }
//}