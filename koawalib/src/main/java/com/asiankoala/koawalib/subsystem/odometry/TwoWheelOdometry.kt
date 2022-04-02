package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.hardware.sensor.KIMU
import com.asiankoala.koawalib.math.*
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.subsystem.DeviceSubsystem
import com.asiankoala.koawalib.util.Logger

class TwoWheelOdometry(
    private val imu: KIMU,
    private val leftEncoder: Encoder,
    private val auxEncoder: Encoder,
    private val TRACK_WIDTH: Double,
    private val PERP_TRACKER: Double,
) : Odometry() {
    private val encoders = listOf(leftEncoder, auxEncoder)
    private var accumulatedAuxPrediction = 0.0
    private var accumRWheel = 0.0

    private var lastAngle = Double.NaN

    override fun updateTelemetry() {
        Logger.addTelemetryData("start pose", startPose)
        Logger.addTelemetryData("curr pose", position)
        Logger.addTelemetryData("left encoder", leftEncoder.position)
        Logger.addTelemetryData("aux encoder", auxEncoder.position)

        val accumAuxScale = auxEncoder.position
        val auxTrackDiff = accumAuxScale - accumulatedAuxPrediction
        Logger.addTelemetryData("accumulated aux", accumAuxScale)
        Logger.addTelemetryData("accumulated aux prediction", accumulatedAuxPrediction)
        Logger.addTelemetryData("accum aux - tracker", auxTrackDiff)
        Logger.addTelemetryData("should increase aux tracker", auxTrackDiff > 0)
    }

    private fun getHeading(): Double {
        return (imu.heading + startPose.heading).wrap
    }

    override fun periodic() {
        if (lastAngle.isNaN()) {
            lastAngle = getHeading()
            return
        }

        Logger.logInfo("before odo update $_position")

        encoders.forEach(Encoder::update)

        val newAngle = getHeading()
        val angleIncrement = (newAngle - lastAngle).wrap
        val auxPrediction = angleIncrement * PERP_TRACKER
        val rX = auxEncoder.delta - auxPrediction

        accumulatedAuxPrediction += auxPrediction

        val rWheelDelta = -(angleIncrement * TRACK_WIDTH - leftEncoder.delta)
        accumRWheel += rWheelDelta
        val deltaY = (leftEncoder.delta + rWheelDelta) / 2.0
        Logger.addTelemetryData("aux encoder", auxEncoder.position)
        Logger.addTelemetryData("accum aux", accumulatedAuxPrediction)
        Logger.addTelemetryData("left encoder", leftEncoder.position)
        Logger.addTelemetryData("rx", rX)
        Logger.addTelemetryData("left - rWheel", 100.0 * (leftEncoder.position - accumRWheel))
        Logger.addTelemetryData("deltaY", deltaY)
        Logger.addTelemetryData("rWheelDelta", rWheelDelta)

        val pointIncrement = updatePoseWithDeltas(_position, leftEncoder.delta, rWheelDelta, rX, deltaY, angleIncrement)

        _position = Pose(_position.point + pointIncrement, newAngle)
        lastAngle = newAngle
    }
}
