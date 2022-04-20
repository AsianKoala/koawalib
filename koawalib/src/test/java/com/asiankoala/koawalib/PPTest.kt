package com.asiankoala.koawalib

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.path.PathBuilder
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.radians
import com.asiankoala.koawalib.path.purepursuit.PurePursuitController
import com.asiankoala.koawalib.path.purepursuit.PurePursuitPath
import com.asiankoala.koawalib.path.purepursuit.Waypoint
import jetbrains.letsPlot.elementRect
import jetbrains.letsPlot.export.ggsave
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.ggplot
import jetbrains.letsPlot.theme

object PPTest {
    @JvmStatic
    fun main(args: Array<String>) {
        var pose = Pose()
        val path = PurePursuitPath(
            listOf(
                Waypoint(0.0, 0.0, 8.0),
                Waypoint(14.0, 14.0, 8.0),
                Waypoint(28.0, 28.0, 8.0),
                Waypoint(36.0, -10.0, 8.0),
                Waypoint(50.0, -20.0, 8.0)
            )
        )



        val xRobot = ArrayList<Double>()
        val yRobot = ArrayList<Double>()
        val dt = 0.7

        while(!path.isFinished) {
            val output = path.update(pose, 2.0)
            val vec = Vector(output.first.x, output.first.y) * dt
            val add = vec.rotate(pose.heading - 90.0.radians)
            pose = Pose(pose.vec + add, output.second)
            xRobot.add(pose.x)
            yRobot.add(pose.y)
        }

        val path2 = PathBuilder(Pose().toPose2d(), startTangent = 45.0.radians)
            .splineToSplineHeading(Pose2d(14.0, 14.0, 0.0), 45.0.radians)
            .splineToSplineHeading(Pose2d(28.0, 28.0, 30.0.radians), 45.0.radians)
            .splineToSplineHeading(Pose2d(36.0, -10.0, (-90.0).radians), (-1.363).radians)
            .splineToSplineHeading(Pose2d(50.0, -20.0, 90.0.radians), (-30.54).radians)
            .build()

        val resolution = xRobot.size
        val xPath = DoubleArray(resolution)
        val yPath = DoubleArray(resolution)
        for(i in 0 until resolution) {
            val s = (i / resolution.toDouble()) * path2.length()
            val point = path2[s]
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