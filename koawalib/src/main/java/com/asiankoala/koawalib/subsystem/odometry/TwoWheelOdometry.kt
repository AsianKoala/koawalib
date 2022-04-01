package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.hardware.sensor.KIMU
import com.asiankoala.koawalib.math.*
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.subsystem.DeviceSubsystem
import com.asiankoala.koawalib.util.Logger

class TwoWheelOdometry(
    private val imu: KIMU,
    private val leftEncoder: Encoder,
    private val perpEncoder: Encoder,
    private val TRACK_WIDTH: Double,
    private val PERP_TRACKER: Double,
) : Odometry() {
    private val encoders = listOf(leftEncoder, perpEncoder)
    private var accumulatedHeading = 0.0
    private var accumulatedPerpPrediction = 0.0

    private var lastAngle = Double.NaN

    override fun updateTelemetry() {
        Logger.addTelemetryData("start pose", startPose.degString)
        Logger.addTelemetryData("curr pose", position.degString)
        Logger.addTelemetryData("left encoder", leftEncoder.position)
        Logger.addTelemetryData("perp encoder", perpEncoder.position)
        Logger.addTelemetryData("accumulated heading", accumulatedHeading.degrees)

        val accumPerpScale = perpEncoder.position
        val perpTrackDiff = accumPerpScale - accumulatedPerpPrediction
        Logger.addTelemetryData("accumulated perp", accumPerpScale)
        Logger.addTelemetryData("accumulated perp prediction", accumulatedPerpPrediction)
        Logger.addTelemetryData("accum perp - tracker", perpTrackDiff)
        Logger.addTelemetryData("should increase perp tracker", perpTrackDiff > 0)
    }

    private fun getHeading(): Double {
        return (imu.heading + startPose.heading).wrap
    }

    override fun localize() {
        if (lastAngle.isNaN()) {
            lastAngle = getHeading()
            return
        }

        encoders.forEach(Encoder::update)

        val newAngle = getHeading()
        val angleIncrement = (newAngle - lastAngle).wrap
        val perpPrediction = angleIncrement * PERP_TRACKER
        val rX = perpEncoder.delta - perpPrediction

        accumulatedHeading += angleIncrement
        accumulatedPerpPrediction += perpPrediction

        val rWheelDelta = -(angleIncrement * TRACK_WIDTH - leftEncoder.delta)
        val deltaY = (leftEncoder.delta - rWheelDelta) / 2.0
        val pointIncrement = updatePoseWithDeltas(_position, leftEncoder.delta, rWheelDelta, rX, deltaY, angleIncrement)

        _position = Pose(_position.point + pointIncrement, newAngle)
        lastAngle = newAngle
    }
}
