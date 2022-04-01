package com.asiankoala.koawalib.roadrunner.drive

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.localization.Localizer
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.subsystem.odometry.Encoder
import com.asiankoala.koawalib.subsystem.odometry.ThreeWheelOdometry

class ThreeWheelOdometryRR(
    leftEncoder: Encoder,
    rightEncoder: Encoder,
    perpEncoder: Encoder,
    TRACK_WIDTH: Double,
    PERP_TRACKER: Double
) : Localizer {
    private val odometry = ThreeWheelOdometry(leftEncoder, rightEncoder, perpEncoder, TRACK_WIDTH, PERP_TRACKER)
    override var poseEstimate: Pose2d
        get() = odometry.position.toPose2d()
        set(value) {
            odometry.startPose = Pose(value)
        }
    override val poseVelocity: Pose2d?
        get() = odometry.velocity.toPose2d()

    override fun update() {
        // ThreeWheelOdometry is updated through CommandScheduler
    }
}