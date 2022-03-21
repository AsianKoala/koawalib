package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.command.CommandOpMode
import com.asiankoala.koawalib.hardware.sensor.KIMU
import com.asiankoala.koawalib.math.MathUtil.degrees
import com.asiankoala.koawalib.math.MathUtil.wrap
import com.asiankoala.koawalib.math.Pose

class TwoWheelOdometry(config: OdoConfig, private val imu: KIMU) : Odometry(config) {
    private var leftOffset = 0.0
    private var auxOffset = 0.0

    private var lastLeftEncoder = 0.0
    private var lastAuxEncoder = 0.0

    private var currLeftEncoder = { config.leftEncoder.position }
    private var currAuxEncoder = { config.auxEncoder.position }

    private var accumulatedHeading = 0.0
    private var accumulatedAuxPrediction = 0.0

    private var lastAngle = Double.NaN

    override fun updateTelemetry() {
        CommandOpMode.logger.addTelemetryData("start pose", startPose.degString)
        CommandOpMode.logger.addTelemetryData("curr pose", position.degString)
        CommandOpMode.logger.addTelemetryData("left encoder", lastLeftEncoder)
        CommandOpMode.logger.addTelemetryData("aux encoder", lastAuxEncoder)
        CommandOpMode.logger.addTelemetryData("left offset", leftOffset)
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
        if(lastAngle.isNaN()) {
            lastAngle = imu.heading
            return
        }

        val currLeft = currLeftEncoder.invoke()
        val currAux = currAuxEncoder.invoke()

        val actualCurrLeft = config.LEFT_SCALAR * (currLeft - leftOffset)
        val actualCurrAux = config.AUX_SCALAR * (currAux - auxOffset)

        val lWheelDelta = (actualCurrLeft - lastLeftEncoder) / config.TICKS_PER_INCH
        val aWheelDelta = (actualCurrAux - lastAuxEncoder) / config.TICKS_PER_INCH

        val newAngle = imu.heading
        val angleIncrement = (newAngle - lastAngle).wrap
        val auxPrediction = angleIncrement * config.AUX_TRACKER
        val rX = aWheelDelta - auxPrediction

        accumulatedHeading += angleIncrement
        accumulatedAuxPrediction += auxPrediction

        val rWheelDelta = -(angleIncrement * config.TRACK_WIDTH - lWheelDelta)
        val deltaY = (lWheelDelta - rWheelDelta) / 2.0
        val pointIncrement = poseExponential(_position, lWheelDelta, rWheelDelta, rX, deltaY, angleIncrement)

        _position = Pose(_position.point + pointIncrement, newAngle)

        lastLeftEncoder = actualCurrLeft
        lastAuxEncoder = actualCurrAux
    }
}