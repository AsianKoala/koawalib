package com.asiankoala.koawalib.subsystem.odometry

import com.acmerobotics.roadrunner.util.NanoClock
import com.asiankoala.koawalib.hardware.sensor.KIMU
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.degrees
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.util.Logger

class KThreeWheelOdometry(
    private val leftEncoder: KEncoder,
    private val rightEncoder: KEncoder,
    private val auxEncoder: KEncoder,
    private val TRACK_WIDTH: Double,
    private val PERP_TRACKER: Double,
    private val imu: KIMU,
    private val secondsBetweenResets: Double = 5.0
) : Odometry() {
    private var encoders = listOf(leftEncoder, rightEncoder, auxEncoder)
    private var accumulatedHeading = 0.0
    private var accumulatedAuxPrediction = 0.0
    private var clock = NanoClock.system()
    private var lastResetTime = clock.seconds()
    private var resetHeading = startPose.heading

    override fun updateTelemetry() {
        Logger.addTelemetryData("start pose", startPose)
        Logger.addTelemetryData("curr pose", pose)
        Logger.addTelemetryData("left encoder", leftEncoder.position)
        Logger.addTelemetryData("right encoder", rightEncoder.position)
        Logger.addTelemetryData("aux encoder", auxEncoder.position)
        Logger.addTelemetryData("accumulated heading", accumulatedHeading.degrees)
    }

    override fun reset() {
        encoders.forEach(KEncoder::zero)
    }

    override fun periodic() {
        encoders.forEach(KEncoder::update)
        shouldReset = ((clock.seconds() - lastResetTime) > secondsBetweenResets) || shouldReset
        if(shouldReset) {
            imu.update()
            resetHeading = (imu.heading + startPose.heading).angleWrap
            Logger.logInfo("reset imu heading: ${imu.heading}")
            pose = Pose(pose.point, resetHeading)
            encoders.forEach(KEncoder::zero)
            lastResetTime = clock.seconds()
            shouldReset = false
            return
        }

        val newAngle = (((rightEncoder.position - leftEncoder.position) / TRACK_WIDTH) + resetHeading).angleWrap

        val headingDelta = (rightEncoder.delta - leftEncoder.delta) / TRACK_WIDTH
        val auxPredicted = headingDelta * PERP_TRACKER
        val auxDelta = auxEncoder.delta - auxPredicted

        accumulatedHeading += headingDelta
        accumulatedAuxPrediction += auxPredicted

        Logger.addTelemetryData("heading delta", headingDelta.degrees)
        Logger.addTelemetryData("aux predicted", auxPredicted)
        Logger.addTelemetryData("aux delta", auxDelta)

        val deltaY = (leftEncoder.delta - rightEncoder.delta) / 2.0
        val pointIncrement = updatePoseWithDeltas(pose, leftEncoder.delta, rightEncoder.delta, auxDelta, deltaY, headingDelta)
        pose = Pose(pose.point + pointIncrement, newAngle)
    }
}
