package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.command.CommandOpMode
import com.asiankoala.koawalib.hardware.sensor.KIMU
import com.asiankoala.koawalib.math.MathUtil.degrees
import com.asiankoala.koawalib.math.MathUtil.wrap
import com.asiankoala.koawalib.math.Pose

class TwoWheelOdometry(config: OdoConfig, private val imu: KIMU) : Odometry(config) {
    private var leftEncoder = Encoder(config.leftEncoder, config.TICKS_PER_INCH)
    private var auxEncoder = Encoder(config.auxEncoder, config.TICKS_PER_INCH)

    private var accumulatedHeading = 0.0
    private var accumulatedAuxPrediction = 0.0

    private var lastAngle = Double.NaN

    override fun updateTelemetry() {
        CommandOpMode.logger.addTelemetryData("start pose", startPose.degString)
        CommandOpMode.logger.addTelemetryData("curr pose", position.degString)
        CommandOpMode.logger.addTelemetryData("left encoder", leftEncoder.currRead)
        CommandOpMode.logger.addTelemetryData("aux encoder", auxEncoder.currRead)
        CommandOpMode.logger.addTelemetryData("left offset", leftEncoder.offset)
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
    }
}