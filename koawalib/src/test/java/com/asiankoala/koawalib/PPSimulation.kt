package com.asiankoala.koawalib

import com.asiankoala.koawalib.math.Point
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.radians
import com.asiankoala.koawalib.path.Path
import com.asiankoala.koawalib.path.Waypoint
import com.qualcomm.robotcore.util.ElapsedTime
import jetbrains.letsPlot.export.ggsave
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.letsPlot

object PPSimulation {
    @JvmStatic
    fun main(args: Array<String>) {
        val waypoints = listOf(
            Waypoint(0.0, 0.0, 0.0),
            Waypoint(0.0, 18.0, 8.0),
            Waypoint(18.0, 18.0, 8.0)
        )

        val path = Path(waypoints)
        val xRobot = ArrayList<Double>()
        val yRobot = ArrayList<Double>()
        var pose = Pose(0.0, 0.0, 90.0.radians)

        val translationalSpeed = 12.0 // inch / s
        val time = ElapsedTime()
        var lastTime = time.seconds()
        while(!path.isFinished) {
            val currTime = time.seconds()
            val dt = currTime - lastTime
            val ret = path.update(pose)

            val pointIncrement = Point(ret.first.x * dt * translationalSpeed, ret.first.y * dt * translationalSpeed)
            val add = pointIncrement.rotate( pose.heading - 90.0.radians)
            pose = Pose(pose.point + add, ret.second)

            lastTime = currTime
            xRobot.add(pose.x)
            yRobot.add(pose.y)
        }
    }
}