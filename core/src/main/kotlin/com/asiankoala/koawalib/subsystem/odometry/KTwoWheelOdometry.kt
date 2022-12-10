package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.hardware.motor.KEncoder
import com.asiankoala.koawalib.hardware.sensor.KIMU
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.angleWrap

// TODO: find what was causing so much drift
class KTwoWheelOdometry(
    private val imu: KIMU,
    private val leftEncoder: KEncoder,
    private val auxEncoder: KEncoder,
    private val TRACK_WIDTH: Double,
    private val PERP_TRACKER: Double,
    startPose: Pose,
) : Odometry(startPose) {
    private val encoders = listOf(leftEncoder, auxEncoder)
    private var accumulatedAuxPrediction = 0.0
    private var accumRWheel = 0.0
    private var lastAngle = Double.NaN

    private fun getHeading(): Double {
        return (imu.heading + startPose.heading).angleWrap
    }

    override fun reset(p: Pose) {
        encoders.forEach(KEncoder::zero)
        pose = p
    }

    override fun updateTelemetry() {
        Logger.addTelemetryData("start pose", startPose)
        Logger.addTelemetryData("curr pose", pose)
        Logger.addTelemetryData("left encoder", leftEncoder.pos)
        Logger.addTelemetryData("aux encoder", auxEncoder.pos)

        val accumAuxScale = auxEncoder.pos
        val auxTrackDiff = accumAuxScale - accumulatedAuxPrediction
        Logger.addTelemetryData("accumulated aux", accumAuxScale)
        Logger.addTelemetryData("accumulated aux prediction", accumulatedAuxPrediction)
        Logger.addTelemetryData("accum aux - tracker", auxTrackDiff)
    }

    override fun periodic() {
        encoders.forEach(KEncoder::update)
        imu.periodic()

        val newAngle = getHeading()
        val angleIncrement = (newAngle - lastAngle).angleWrap
        val auxPrediction = angleIncrement * PERP_TRACKER
        val rX = auxEncoder.delta - auxPrediction

        accumulatedAuxPrediction += auxPrediction

        val rWheelDelta = -(angleIncrement * TRACK_WIDTH - leftEncoder.delta)
        accumRWheel += rWheelDelta
        val deltaY = (leftEncoder.delta + rWheelDelta) / 2.0

        val pointIncrement = updatePoseWithDeltas(pose, leftEncoder.delta, rWheelDelta, rX, deltaY, angleIncrement)

        pose = Pose(pose.vec + pointIncrement, newAngle)
        lastAngle = newAngle
        savePose(pose)
    }
}
