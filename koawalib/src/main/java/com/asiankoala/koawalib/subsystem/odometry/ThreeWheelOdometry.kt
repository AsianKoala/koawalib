package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.command.CommandOpMode
import com.asiankoala.koawalib.math.MathUtil.degrees
import com.asiankoala.koawalib.math.MathUtil.wrap
import com.asiankoala.koawalib.math.Pose

class ThreeWheelOdometry(config: OdoConfig) : Odometry(config) {
    private var leftEncoder = Encoder(config.leftEncoder, config.TICKS_PER_INCH)
    private var rightEncoder = Encoder(config.rightEncoder, config.TICKS_PER_INCH)
    private var auxEncoder = Encoder(config.auxEncoder, config.TICKS_PER_INCH)
    private var encoders = listOf(leftEncoder, rightEncoder, auxEncoder)
    private var accumulatedHeading = 0.0
    private var accumulatedAuxPrediction = 0.0

    override fun updateTelemetry() {
        CommandOpMode.logger.addTelemetryData("start pose", startPose.degString)
        CommandOpMode.logger.addTelemetryData("curr pose", position.degString)
        CommandOpMode.logger.addTelemetryData("left encoder", leftEncoder.read())
        CommandOpMode.logger.addTelemetryData("right encoder", rightEncoder.read())
        CommandOpMode.logger.addTelemetryData("aux encoder", auxEncoder.read())
        CommandOpMode.logger.addTelemetryData("left offset", leftEncoder.offset)
        CommandOpMode.logger.addTelemetryData("right offset", rightEncoder.offset)
        CommandOpMode.logger.addTelemetryData("aux offset", auxEncoder.offset)
        CommandOpMode.logger.addTelemetryData("accumulated heading", accumulatedHeading.degrees)
        val accumAuxScale = auxEncoder.currRead / config.TICKS_PER_INCH
        val auxTrackDiff = accumAuxScale - accumulatedAuxPrediction
        CommandOpMode.logger.addTelemetryData("accumulated aux", accumAuxScale)
        CommandOpMode.logger.addTelemetryData("accumulated aux prediction", accumulatedAuxPrediction)
        CommandOpMode.logger.addTelemetryData("accum aux - tracker", auxTrackDiff)
        CommandOpMode.logger.addTelemetryData("should increase aux tracker", auxTrackDiff > 0)
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