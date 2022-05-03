package com.asiankoala.koawalib

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.kinematics.Kinematics
import com.asiankoala.koawalib.math.radians

object KinematicsTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val pose = Pose2d(heading = 90.0.radians)
        val vel = Pose2d(0.0,5.0)
        val accel = Pose2d(0.0, 1.0)
        val robotVel = Kinematics.fieldToRobotVelocity(pose, vel)
        val robotAccel = Kinematics.fieldToRobotAcceleration(pose, vel, accel)
        println(robotVel)
        println(robotAccel)
    }
}