package com.asiankoala.koawalib

import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.path.PathBuilder
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.radians
import com.asiankoala.koawalib.path.gvf.GVFController
import jetbrains.letsPlot.*
import jetbrains.letsPlot.export.ggsave
import jetbrains.letsPlot.geom.geomPoint
import kotlin.math.*

object GVFTest {

    @JvmStatic
    fun main(args: Array<String>) {
        var pose = Pose(2.0, 12.0, 90.0.radians)
        val path = PathBuilder(Pose().toPose2d(), startTangent = 0.0)
            .splineTo(Vector2d(14.0, 14.0), 45.0.radians)
            .splineTo(Vector2d(28.0, 28.0), 30.0.radians)
            .splineTo(Vector2d(36.0, -10.0), (-120.0).radians)
            .splineTo(Vector2d(50.0, -20.0), (-30.0).radians)
            .splineTo(Vector2d(70.0, 70.0), 0.0)
            .splineTo(Vector2d(), 180.0.radians)
            .build()

        val controller = GVFController(path, 0.6, 1.0, 12.0, 0.5)

        val xRobot = ArrayList<Double>()
        val yRobot = ArrayList<Double>()
        val dt = 0.7

        while(!controller.isFinished) {
            val output = controller.update(pose)
            pose = pose.plusWrap(output.first * dt)
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

    private fun dampFunc(error: Double): Double {
        val power = 2
        val horizontalStretch = 5
        return abs(error / horizontalStretch).pow(power) * sign(error) /
                    (1.0 + abs(error / horizontalStretch).pow(power))
    }

    private fun atanFunc(error: Double): Double {
        val power = 2
        val horizontalStretch = 5
        return atan(abs(error / horizontalStretch).pow(power) * sign(error)) /
                (PI / 2.0)
    }
}