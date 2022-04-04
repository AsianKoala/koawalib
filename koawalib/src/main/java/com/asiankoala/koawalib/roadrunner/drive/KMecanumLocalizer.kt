package com.asiankoala.koawalib.roadrunner.drive

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.kinematics.Kinematics
import com.acmerobotics.roadrunner.kinematics.MecanumKinematics
import com.acmerobotics.roadrunner.localization.Localizer
import com.acmerobotics.roadrunner.util.Angle
import com.asiankoala.koawalib.hardware.sensor.KIMU
import com.asiankoala.koawalib.subsystem.odometry.KEncoder

@Suppress("unused")
class MecanumLocalizer constructor(
    leftFrontEncoder: KEncoder,
    leftRearEncoder: KEncoder,
    rightRearEncoder: KEncoder,
    rightFrontEncoder: KEncoder,
    private val imu: KIMU,
    private val driveConstants: DriveConstants
) : Localizer {
    private var _poseEstimate = Pose2d()
    override var poseEstimate: Pose2d
        get() = _poseEstimate
        set(value) {
            lastWheelPositions = emptyList()
            lastExtHeading = Double.NaN
            _poseEstimate = value
        }
    override var poseVelocity: Pose2d = Pose2d()
        private set
    private var lastWheelPositions = emptyList<Double>()
    private var lastExtHeading = Double.NaN
    private val encoders = listOf(leftFrontEncoder, leftRearEncoder, rightRearEncoder, rightFrontEncoder)
    private val encoderPositions: List<Double>
        get() = encoders.map { it.position }
    private val encoderVelocities: List<Double>
        get() = encoders.map { it.velocity }

    override fun update() {
        encoders.forEach(KEncoder::update)

        val wheelPositions = encoderPositions
        val extHeading = imu.heading

        if (lastWheelPositions.isNotEmpty()) {
            val wheelDeltas = wheelPositions
                .zip(lastWheelPositions)
                .map { it.first - it.second }
            val robotPoseDelta = MecanumKinematics.wheelToRobotVelocities(
                wheelDeltas,
                driveConstants.TRACK_WIDTH,
                driveConstants.TRACK_WIDTH,
                1.0
            )
            val finalHeadingDelta = Angle.normDelta(extHeading - lastExtHeading)
            _poseEstimate = Kinematics.relativeOdometryUpdate(
                _poseEstimate,
                Pose2d(robotPoseDelta.vec(), finalHeadingDelta)
            )
        }

        val wheelVelocities = encoderVelocities
        val extHeadingVel = imu.headingVel
        poseVelocity = MecanumKinematics.wheelToRobotVelocities(
            wheelVelocities,
            driveConstants.TRACK_WIDTH,
            driveConstants.TRACK_WIDTH,
            1.0
        )
        poseVelocity = Pose2d(poseVelocity!!.vec(), extHeadingVel)

        lastWheelPositions = wheelPositions
        lastExtHeading = extHeading
    }
}
