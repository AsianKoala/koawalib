package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.math.*
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.util.Logger

class ThreeWheelOdometry(config: OdoConfig) : Odometry(config) {
    private var leftEncoder = Encoder(config.leftEncoder, config.TICKS_PER_INCH)
    private var rightEncoder = Encoder(config.rightEncoder, config.TICKS_PER_INCH)
    private var auxEncoder = Encoder(config.auxEncoder, config.TICKS_PER_INCH)
    private var encoders = listOf(leftEncoder, rightEncoder, auxEncoder)
    private var accumulatedHeading = 0.0
    private var accumulatedAuxPrediction = 0.0

    override fun updateTelemetry() {
        Logger.addTelemetryData("start pose", startPose.degString)
        Logger.addTelemetryData("curr pose", position.degString)
        Logger.addTelemetryData("left encoder", leftEncoder.currRead)
        Logger.addTelemetryData("right encoder", rightEncoder.currRead)
        Logger.addTelemetryData("aux encoder", auxEncoder.currRead)
        Logger.addTelemetryData("left offset", leftEncoder.offset)
        Logger.addTelemetryData("right offset", rightEncoder.offset)
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
        encoders.forEach(Encoder::read)

        val newAngle = (((leftEncoder.currRead - rightEncoder.currRead) / config.TRACK_WIDTH) + startPose.heading).wrap

        val angleIncrement = (leftEncoder.delta - rightEncoder.delta) / config.TRACK_WIDTH
        val auxPrediction = angleIncrement * config.AUX_TRACKER
        val rX = auxEncoder.delta - auxPrediction

        accumulatedHeading += angleIncrement
        accumulatedAuxPrediction += auxPrediction

        val deltaY = (leftEncoder.delta - rightEncoder.delta) / 2.0
        val pointIncrement = poseExponential(_position, leftEncoder.delta, rightEncoder.delta, rX, deltaY, angleIncrement)

        _position = Pose(_position.point + pointIncrement, newAngle)
    }
}
