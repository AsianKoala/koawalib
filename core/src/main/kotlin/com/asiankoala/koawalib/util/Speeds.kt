package com.asiankoala.koawalib.util

import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.subsystem.drive.KMecanumDrive
import kotlin.math.PI

class Speeds {
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

    fun getWheels(heading: Double) = KMecanumDrive.mecKinematics(getRobotCentric(heading))

    fun setWheels(wheels: List<Double>, heading: Double) {
        val (_, bl, br, fr) = wheels
        val x = (br - fr) / 2
        val h = (fr - bl) / 2
        val y = br - x - h
        setRobotCentric(Pose(x, y, h), heading)
    }
}
