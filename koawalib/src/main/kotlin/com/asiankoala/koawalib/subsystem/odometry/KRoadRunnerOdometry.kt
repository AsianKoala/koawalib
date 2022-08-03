package com.asiankoala.koawalib.subsystem.odometry

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.localization.ThreeTrackingWheelLocalizer
import com.asiankoala.koawalib.hardware.motor.KEncoder
import com.asiankoala.koawalib.math.Pose

class KRoadRunnerOdometry(
    private val leftEncoder: KEncoder,
    private val rightEncoder: KEncoder,
    private val auxEncoder: KEncoder,
    private val LATERAL_DISTANCE: Double,
    private val FORWARD_OFFSET: Double
) : Odometry() {
    private val rrOdo = object : ThreeTrackingWheelLocalizer(
        listOf(
            Pose2d(0.0, LATERAL_DISTANCE / 2, 0.0),
            Pose2d(0.0, -LATERAL_DISTANCE / 2, 0.0),
            Pose2d(FORWARD_OFFSET, 0.0, Math.toRadians(90.0))
        )
    ) {
        override fun getWheelPositions(): List<Double> {
            return listOf(
                leftEncoder.pos,
                rightEncoder.pos,
                auxEncoder.pos
            )
        }
    }

    override fun updateTelemetry() {

    }

    override fun reset() {

    }

    override fun periodic() {
        pose = Pose(rrOdo.poseEstimate)
    }
}