package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.hardware.sensor.KIMU
import com.asiankoala.koawalib.math.MathUtil.degrees
import com.asiankoala.koawalib.math.MathUtil.wrap
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.util.Logger

class TwoWheelOdometry(config: OdoConfig, private val imu: KIMU) : Odometry(config) {
    private var leftEncoder = Encoder(config.leftEncoder, config.TICKS_PER_INCH)
    private var auxEncoder = Encoder(config.auxEncoder, config.TICKS_PER_INCH)

    private var accumulatedHeading = 0.0
    private var accumulatedAuxPrediction = 0.0

    private var lastAngle = Double.NaN

    override fun updateTelemetry() {
        Logger.addTelemetryData("start pose", startPose.degString)
        Logger.addTelemetryData("curr pose", position.degString)
        Logger.addTelemetryData("left encoder", leftEncoder.currRead)
        Logger.addTelemetryData("aux encoder", auxEncoder.currRead)
        Logger.addTelemetryData("left offset", leftEncoder.offset)
        Logger.addTelemetryData("aux offset", auxEncoder.offset)
        Logger.addTelemetryData("accumulated heading", accumulatedHeading.degrees)

        val accumAuxScale = auxEncoder.currRead / config.TICKS_PER_INCH
        val auxTrackDiff = accumAuxScale - accumulatedAuxPrediction
        Logger.addTelemetryData("accumulated aux", accumAuxScale)
        Logger.addTelemetryData("accumulated aux prediction", accumulatedAuxPrediction)
        Logger.addTelemetryData("accum aux - tracker", auxTrackDiff)
        Logger.addTelemetryData("should increase aux tracker", auxTrackDiff > 0)
    }

    override fun localize() {
        if(lastAngle.isNaN()) {
            lastAngle = imu.heading
            return
        }

        leftEncoder.read()
        auxEncoder.read()

        val newAngle = imu.heading
        val angleIncrement = (newAngle - lastAngle).wrap
        val auxPrediction = angleIncrement * config.AUX_TRACKER
        val rX = auxEncoder.delta - auxPrediction

        accumulatedHeading += angleIncrement
        accumulatedAuxPrediction += auxPrediction

        val rWheelDelta = -(angleIncrement * config.TRACK_WIDTH - leftEncoder.delta)
        val deltaY = (leftEncoder.delta - rWheelDelta) / 2.0
        val pointIncrement = poseExponential(_position, leftEncoder.delta, rWheelDelta, rX, deltaY, angleIncrement)

        _position = Pose(_position.point + pointIncrement, newAngle)
        lastAngle = newAngle
    }
}