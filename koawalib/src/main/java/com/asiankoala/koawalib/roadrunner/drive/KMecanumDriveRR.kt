package com.asiankoala.koawalib.roadrunner.drive

import com.acmerobotics.roadrunner.control.PIDCoefficients
import com.acmerobotics.roadrunner.drive.MecanumDrive
import com.acmerobotics.roadrunner.followers.HolonomicPIDVAFollower
import com.acmerobotics.roadrunner.followers.TrajectoryFollower
import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.localization.Localizer
import com.acmerobotics.roadrunner.trajectory.Trajectory
import com.acmerobotics.roadrunner.trajectory.TrajectoryBuilder
import com.acmerobotics.roadrunner.trajectory.constraints.*
import com.asiankoala.koawalib.hardware.motor.KMotor
import com.asiankoala.koawalib.math.radians
import com.asiankoala.koawalib.roadrunner.trajectorysequence.TrajectorySequence
import com.asiankoala.koawalib.roadrunner.trajectorysequence.TrajectorySequenceBuilder
import com.asiankoala.koawalib.roadrunner.trajectorysequence.TrajectorySequenceRunnerCancelable
import com.asiankoala.koawalib.subsystem.Subsystem
import com.asiankoala.koawalib.util.Logger


// NOTE: ONLY SUPPORTS FEEDFORWARD ROADRUNNER FOR NOW
// I'LL CHANGE IT WHEN I HAVE TIME
// TODO
open class KMecanumDriveRR(
    private val driveConstants: DriveConstants,
    frontLeft: KMotor,
    rearLeft: KMotor,
    rearRight: KMotor,
    frontRight: KMotor,
    driveLocalizer: Localizer,
    TRANSLATIONAL_PID: PIDCoefficients,
    HEADING_PID: PIDCoefficients,
) : MecanumDrive(driveConstants.kV, driveConstants.kA, driveConstants.kStatic, driveConstants.TRACK_WIDTH), Subsystem {
    private val velConstraint: TrajectoryVelocityConstraint =
        getVelocityConstraint(driveConstants.MAX_VEL, driveConstants.MAX_ANG_VEL, driveConstants.TRACK_WIDTH)
    private val accelConstraint: TrajectoryAccelerationConstraint =
        getAccelerationConstraint(driveConstants.MAX_ACCEL)

    private val follower: TrajectoryFollower = HolonomicPIDVAFollower(TRANSLATIONAL_PID, TRANSLATIONAL_PID, HEADING_PID,
        Pose2d(0.5, 0.5, 5.0.radians), 0.5)

    private val trajectorySequenceRunner = TrajectorySequenceRunnerCancelable(follower, HEADING_PID)

    private val motors = listOf(frontLeft, rearLeft, rearRight, frontRight)

    fun trajectoryBuilder(startPose: Pose2d): TrajectoryBuilder {
        return TrajectoryBuilder(startPose, false, velConstraint, accelConstraint)
    }

    fun trajectoryBuilder(startPose: Pose2d, reversed: Boolean): TrajectoryBuilder? {
        return TrajectoryBuilder(startPose, reversed, velConstraint, accelConstraint)
    }

    fun trajectoryBuilder(startPose: Pose2d, startHeading: Double): TrajectoryBuilder? {
        return TrajectoryBuilder(startPose, startHeading, velConstraint, accelConstraint)
    }

    private fun getVelocityConstraint(
        maxVel: Double,
        maxAngularVel: Double,
        trackWidth: Double
    ): TrajectoryVelocityConstraint {
        return MinVelocityConstraint(
            listOf(
                AngularVelocityConstraint(maxAngularVel),
                MecanumVelocityConstraint(maxVel, trackWidth)
            )
        )
    }

    private fun getAccelerationConstraint(maxAccel: Double): TrajectoryAccelerationConstraint {
        return ProfileAccelerationConstraint(maxAccel)
    }

    fun trajectorySequenceBuilder(startPose: Pose2d?): TrajectorySequenceBuilder {
        return TrajectorySequenceBuilder(
            startPose!!,
            velConstraint, accelConstraint,
            driveConstants.MAX_ANG_VEL, driveConstants.MAX_ANG_ACCEL
        )
    }

    fun turnAsync(angle: Double) {
        trajectorySequenceRunner!!.followTrajectorySequenceAsync(
            trajectorySequenceBuilder(poseEstimate)
                .turn(angle)
                .build()
        )
    }

    fun turn(angle: Double) {
        turnAsync(angle)
        waitForIdle()
    }

    fun followTrajectoryAsync(trajectory: Trajectory) {
        trajectorySequenceRunner.followTrajectorySequenceAsync(
            trajectorySequenceBuilder(trajectory.start())
                .addTrajectory(trajectory)
                .build()
        )
    }

    fun followTrajectory(trajectory: Trajectory) {
        followTrajectoryAsync(trajectory)
        waitForIdle()
    }

    fun followTrajectorySequenceAsync(trajectorySequence: TrajectorySequence?) {
        trajectorySequenceRunner.followTrajectorySequenceAsync(trajectorySequence)
    }

    fun followTrajectorySequence(trajectorySequence: TrajectorySequence?) {
        followTrajectorySequenceAsync(trajectorySequence)
        waitForIdle()
    }

    fun breakFollowing() {
        trajectorySequenceRunner.breakFollowing()
    }

    fun getLastError(): Pose2d? {
        return trajectorySequenceRunner.lastPoseError
    }

    fun update() {
        updatePoseEstimate()
        val signal = trajectorySequenceRunner.update(poseEstimate, poseVelocity)
        signal?.let { setDriveSignal(it) }
    }

    fun waitForIdle() {
        while (!Thread.currentThread().isInterrupted && isBusy()) update()
    }

    fun isBusy(): Boolean {
        return trajectorySequenceRunner.isBusy
    }

    override val rawExternalHeading: Double
        get() {
            Logger.logWarning("rr drive rawExternalHeading called")
            return Double.NaN
        }

    override fun getWheelPositions(): List<Double> {
        Logger.logWarning("rr drive getWheelPositions called")
        return DoubleArray(4) { Double.NaN }.toList()
    }

    override fun setMotorPowers(
        frontLeft: Double,
        rearLeft: Double,
        rearRight: Double,
        frontRight: Double
    ) {
        val powers = listOf(frontLeft, rearLeft, rearRight, frontRight)
        motors.forEachIndexed { index, kMotor -> kMotor.setSpeed(powers[index]) }
    }

    init {
        register()
        localizer = driveLocalizer
    }
}