package com.asiankoala.koawalib

import com.asiankoala.koawalib.math.Point
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.radians
import com.asiankoala.koawalib.path.gvf.GVFController
import com.asiankoala.koawalib.path.gvf.SplinePath
import com.qualcomm.robotcore.util.ElapsedTime
import jetbrains.letsPlot.export.ggsave
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.letsPlot
import kotlin.math.PI

object SplineInterpolationTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val path = SplinePath(arrayOf(
            Pose(0.0, 0.0, 0.0),
            Pose(5.0, 5.0, PI / 2),
            Pose(10.0, 10.0, 0.0),
            Pose(20.0, 10.0, 0.0),
            Pose(25.0, 5.0, 0.0),
            Pose(40.0, -20.0, 0.0.radians),
            Pose(10.0, -20.0, 0.0.radians)
        ))
        println(path.length())
        val controller = GVFController(path, 3.0, 3.0)
        val dt = 10.0 / 1000.0

        val xRobot = ArrayList<Double>()
        val yRobot = ArrayList<Double>()
        var position = Point(0.00, -0.0)
        var heading = Point.fromAngle(0.0 * PI / 2)
        val time = ElapsedTime()

        while(time.seconds() < 5.0) {
            val pose = Pose(position.x, position.y, heading.atan2)
            heading = controller.vectorControl(pose)
            position += heading.scale(dt)

            xRobot.add(position.x)
            yRobot.add(position.y)
        }

        val xPath = DoubleArray(201)
        val yPath = DoubleArray(201)
        for (i in 0..200) {
            val t = i / 205.0
            val xy = path.calculatePoint(t)
            xPath[i] = xy.x
            yPath[i] = xy.y
        }
        println("$time")

        plot(xPath, yPath)
    }

    fun plot(xs: DoubleArray, ys: DoubleArray) {
        val data = mapOf<String, Any>("x" to xs, "y" to ys)
        val fig = letsPlot(data) + geomPoint(
            color = "dark-green",
            size = 1.0
        ) { x = "x"; y = "y" }

        ggsave(fig, "plot.png")
    }
}