package com.asiankoala.koawalib

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.path.PathBuilder
import com.acmerobotics.roadrunner.util.NanoClock
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.math.d
import com.asiankoala.koawalib.math.radians
import com.asiankoala.koawalib.path.gvf.GVFController
import com.qualcomm.robotcore.util.ElapsedTime
import jetbrains.letsPlot.*
import jetbrains.letsPlot.export.ggsave
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.geom.geomRect

object GVFTest {

    @JvmStatic
    fun main(args: Array<String>) {
        var pose = Pose(2.0, 5.0, 90.0.radians)
        val t = NanoClock.system()
        val startTime = t.seconds()
        val path = PathBuilder(Pose().toPose2d(), startTangent = 0.0)
            .splineToSplineHeading(Pose2d(14.0, 14.0, 0.0), 0.0)
            .splineToSplineHeading(Pose2d(28.0, 28.0, 30.0.radians), 30.0.radians)
            .splineToSplineHeading(Pose2d(36.0, -10.0, (-90.0).radians), (-90.0).radians)
            .splineToSplineHeading(Pose2d(50.0, -20.0, 90.0.radians), 30.0.radians)
            .build()

        val endTime  = t.seconds()
        println(endTime - startTime)

        val controller = GVFController(path, 0.09, 1.0, 1/5.0, 0.5)
        val xRobot = ArrayList<Double>()
        val yRobot = ArrayList<Double>()
        val dt = 0.7

        while(!controller.finished) {
            val output = controller.update(pose)
            pose = pose.plusWrap(output.scale(dt))
            xRobot.add(pose.x)
            yRobot.add(pose.y)
        }

        val resolution = xRobot.size
        val xPath = DoubleArray(resolution)
        val yPath = DoubleArray(resolution)
        for(i in 0 until resolution) {
            val s = (i / resolution.toDouble()) * path.length()
            val point = path[s]
            xPath[i] = point.x
            yPath[i] = point.y
        }

        val data = mapOf<String, Any>("x" to xRobot, "y" to yRobot, "xp" to xPath, "yp" to yPath)
        val fig = ggplot(data) +
                geomPoint(color = "red", size = 1.0) { x = "x"; y = "y" } +
                geomPoint(color = "dark-green", size = 1.0) { x = "xp"; y = "yp"} +
                theme(plotBackground = elementRect(fill = "black"))
        ggsave(fig, "plot.png")
    }
}