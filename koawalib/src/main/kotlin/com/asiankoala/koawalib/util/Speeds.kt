package com.asiankoala.koawalib.util

import com.asiankoala.koawalib.math.Pose
import kotlin.math.PI

class Speeds {
    // internally field centric
    private var internalSpeed = Pose()

    private fun Pose.convert(h: Double) = Pose(this.vec.rotate(PI / 2.0 - h), heading)

    fun getFieldCentric(): Pose = internalSpeed

    fun setFieldCentric(speeds: Pose) {
        internalSpeed = speeds
    }

    fun getRobotCentric(heading: Double) = internalSpeed.convert(heading)

    fun setRobotCentric(speeds: Pose, heading: Double) {
        internalSpeed = speeds.convert(heading)
    }

    fun getWheels(heading: Double): List<Double> {
        val drivePowers = getRobotCentric(heading)
        val fl = drivePowers.y + drivePowers.x - drivePowers.heading
        val bl = drivePowers.y - drivePowers.x - drivePowers.heading
        val br = drivePowers.y + drivePowers.x + drivePowers.heading
        val fr = drivePowers.y - drivePowers.x + drivePowers.heading
        return listOf(fl, bl, br, fr)
    }

    fun setWheels(wheels: List<Double>, heading: Double) {
        val (_, bl, br, fr) = wheels
        val x = (br - fr) / 2
        val h = (fr - bl) / 2
        val y = br - x - h
        setRobotCentric(Pose(x, y, h), heading)
    }
}
