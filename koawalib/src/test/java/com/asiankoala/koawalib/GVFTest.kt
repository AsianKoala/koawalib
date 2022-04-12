package com.asiankoala.koawalib

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.path.PathBuilder
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.d
import com.asiankoala.koawalib.math.radians
import com.asiankoala.koawalib.path.gvf.GVFController
import com.asiankoala.koawalib.path.gvf.toPose
import com.qualcomm.robotcore.util.ElapsedTime
import jetbrains.letsPlot.export.ggsave
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.letsPlot
import jetbrains.letsPlot.positionJitter

object GVFTest {

    @JvmStatic
    fun main(args: Array<String>) {
        var pose = Pose(2.0, -5.0)
        val path = PathBuilder(Pose().toPose2d())
            .splineToSplineHeading(Pose2d(14.0, 14.0, 0.0), 0.0)
            .splineToSplineHeading(Pose2d(28.0, 28.0, 90.0.radians), 90.0.radians)
            .build()

        val controller = GVFController(1.0, 1.0, 1/5.0)
        val xRobot = ArrayList<Double>()
        val yRobot = ArrayList<Double>()
        val dt = 1.0

        while(!controller.finished) {
            val output = controller.update(pose, path)
            pose = Pose(pose.x + output.x * dt, pose.y + output.y * dt, pose.heading)
            xRobot.add(pose.x)
            yRobot.add(pose.y)
        }

//        val resolution = 100
//        val xPath = DoubleArray(resolution+1)
//        val yPath = DoubleArray(resolution+1)
//        for(i in 0..resolution) {
//            val s = (i / resolution.d) * path.length()
//            val point = path[s]
//            xPath[i] = point.x
//            yPath[i] = point.y
//        }
//
//        xRobot.addAll(xPath.toTypedArray())
//        yRobot.addAll(yPath.toTypedArray())


        val data = mapOf<String, Any>("x" to xRobot, "y" to yRobot)
        val fig = letsPlot(data) + geomPoint(color = "blue", size = 0.7, alpha=0.8) { x = "x"; y = "y" }

        ggsave(fig, "plot.png")
    }
}