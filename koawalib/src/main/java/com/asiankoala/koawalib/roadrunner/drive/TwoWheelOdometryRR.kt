package com.asiankoala.koawalib.roadrunner.drive

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.localization.Localizer
import com.asiankoala.koawalib.hardware.sensor.KIMU
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.subsystem.odometry.KEncoder
import com.asiankoala.koawalib.subsystem.odometry.KTwoWheelOdo

// internally use koawalib's twowheelodometry
@Suppress("unused")
class TwoWheelOdometryRR(
    imu: KIMU,
    leftEncoder: KEncoder,
    perpEncoder: KEncoder,
    TRACK_WIDTH: Double,
    PERP_TRACKER: Double,
    ) : Localizer {
    private val odometry = KTwoWheelOdo(imu, leftEncoder, perpEncoder, TRACK_WIDTH, PERP_TRACKER)
    override var poseEstimate: Pose2d
        get() = odometry.pose.toPose2d()
        set(value) {
            odometry.startPose = Pose(value)
        }
    override val poseVelocity: Pose2d
        get() = odometry.velocity.toPose2d()

    override fun update() {
        // TwoWheelOdometry is updated through CommandScheduler
    }
}