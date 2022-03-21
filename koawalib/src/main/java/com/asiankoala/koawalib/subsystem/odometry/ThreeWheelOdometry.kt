package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.command.CommandOpMode
import com.asiankoala.koawalib.math.MathUtil.degrees
import com.asiankoala.koawalib.math.MathUtil.wrap
import com.asiankoala.koawalib.math.Pose

class ThreeWheelOdometry(config: OdoConfig) : Odometry(config) {
    private var leftOffset = 0.0
    private var rightOffset = 0.0
    private var auxOffset = 0.0

    private var lastLeftEncoder = 0.0
    private var lastRightEncoder = 0.0
    private var lastAuxEncoder = 0.0

    private var currLeftEncoder = { config.leftEncoder.position }
    private var currRightEncoder = { config.rightEncoder.position }
    private var currAuxEncoder = { config.auxEncoder.position }

    private var accumulatedHeading = 0.0
    private var accumulatedAuxPrediction = 0.0

    override fun updateTelemetry() {
        CommandOpMode.logger.addTelemetryData("start pose", startPose.degString)
        CommandOpMode.logger.addTelemetryData("curr pose", position.degString)
        CommandOpMode.logger.addTelemetryData("left encoder", lastLeftEncoder)
        CommandOpMode.logger.addTelemetryData("right encoder", lastRightEncoder)
        CommandOpMode.logger.addTelemetryData("aux encoder", lastAuxEncoder)
        CommandOpMode.logger.addTelemetryData("left offset", leftOffset)
        CommandOpMode.logger.addTelemetryData("right offset", rightOffset)
        CommandOpMode.logger.addTelemetryData("aux offset", auxOffset)
        CommandOpMode.logger.addTelemetryData("accumulated heading", accumulatedHeading.degrees)

        val accumAuxScale = lastAuxEncoder / config.TICKS_PER_INCH
        val auxTrackDiff = accumAuxScale - accumulatedAuxPrediction
        CommandOpMode.logger.addTelemetryData("accumulated aux", accumAuxScale)
        CommandOpMode.logger.addTelemetryData("accumulated aux prediction", accumulatedAuxPrediction)
        CommandOpMode.logger.addTelemetryData("accum aux - tracker", auxTrackDiff)
        CommandOpMode.logger.addTelemetryData("should increase aux tracker", auxTrackDiff > 0)
    }

    override fun localize() {
        val currLeft = currLeftEncoder.invoke()
        val currRight = currRightEncoder.invoke()
        val currAux = currAuxEncoder.invoke()

        val actualCurrLeft = config.LEFT_SCALAR * (currLeft - leftOffset)
        val actualCurrRight = config.RIGHT_SCALAR * (currRight - rightOffset)
        val actualCurrAux = config.AUX_SCALAR * (currAux - auxOffset)

        val lWheelDelta = (actualCurrLeft - lastLeftEncoder) / config.TICKS_PER_INCH
        val rWheelDelta = (actualCurrRight - lastRightEncoder) / config.TICKS_PER_INCH
        val aWheelDelta = (actualCurrAux - lastAuxEncoder) / config.TICKS_PER_INCH

        val leftTotal = actualCurrLeft / config.TICKS_PER_INCH
        val rightTotal = actualCurrRight / config.TICKS_PER_INCH
        val newAngle = (((leftTotal - rightTotal) / config.TRACK_WIDTH) + startPose.heading).wrap

        val angleIncrement = (lWheelDelta - rWheelDelta) / config.TRACK_WIDTH
        val auxPrediction = angleIncrement * config.AUX_TRACKER
        val rX = aWheelDelta - auxPrediction

        accumulatedHeading += angleIncrement
        accumulatedAuxPrediction += auxPrediction

        val deltaY = (lWheelDelta - rWheelDelta) / 2.0
        val pointIncrement = poseExponential(_position, lWheelDelta, rWheelDelta, rX, deltaY, angleIncrement)

        _position = Pose(_position.point + pointIncrement, newAngle)

        lastLeftEncoder = actualCurrLeft
        lastRightEncoder = actualCurrRight
        lastAuxEncoder = actualCurrAux
    }
}