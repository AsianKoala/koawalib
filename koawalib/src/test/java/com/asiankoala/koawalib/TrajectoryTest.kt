package com.asiankoala.koawalib

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.path.PathBuilder
import com.acmerobotics.roadrunner.profile.MotionProfileGenerator
import com.acmerobotics.roadrunner.profile.MotionState
import com.acmerobotics.roadrunner.trajectory.Trajectory
import com.asiankoala.koawalib.math.radians
import kotlin.math.sqrt

object TrajectoryTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val profile = MotionProfileGenerator.generateSimpleMotionProfile(
            MotionState(0.0, 0.0),
            MotionState(200.0, 0.0),
            50.0,
            50.0
        )

        println(profile.duration())

        var v = Vector2d(120.0, 0.0)
        v = v.rotated(45.0.radians)
        val path = PathBuilder(Pose2d())
            .splineTo(Vector2d(v.x, v.y), 0.0)
            .build()

        val traj = Trajectory(path, profile)
        val t = 2.0

        println(path.deriv(t))
        println(traj.profile[t])
        println(traj.velocity(t))
        println(traj.acceleration(t))
    }
}