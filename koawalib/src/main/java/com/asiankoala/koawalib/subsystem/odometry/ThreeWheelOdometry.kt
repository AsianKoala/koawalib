package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.math.*
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.util.Logger

class ThreeWheelOdometry(config: OdoConfig) : Odometry(config) {
    private var leftEncoder = Encoder(config.leftEncoder, config.TICKS_PER_INCH)
    private var rightEncoder = Encoder(config.rightEncoder, config.TICKS_PER_INCH)
    private var perpEncoder = Encoder(config.perpEncoder, config.TICKS_PER_INCH)
    private var encoders = listOf(leftEncoder, rightEncoder, perpEncoder)
    private var accumulatedHeading = 0.0
    private var accumulatedPerpPrediction = 0.0

    override fun updateTelemetry() {
        Logger.addTelemetryData("start pose", startPose.degString)
        Logger.addTelemetryData("curr pose", position.degString)
        Logger.addTelemetryData("left encoder", leftEncoder.currRead)
        Logger.addTelemetryData("right encoder", rightEncoder.currRead)
        Logger.addTelemetryData("perp encoder", perpEncoder.currRead)
        Logger.addTelemetryData("left offset", leftEncoder.offset)
        Logger.addTelemetryData("right offset", rightEncoder.offset)
        Logger.addTelemetryData("perp offset", perpEncoder.offset)
        Logger.addTelemetryData("accumulated heading", accumulatedHeading.degrees)

        val accumPerp = perpEncoder.currRead
        val perpPredictDelta = accumPerp - accumulatedPerpPrediction
        Logger.addTelemetryData("accumulated perp", accumPerp)
        Logger.addTelemetryData("accumulated perp prediction", accumulatedPerpPrediction)
        Logger.addTelemetryData("accum perp - tracker", perpPredictDelta)
        Logger.addTelemetryData("should increase perp tracker", perpPredictDelta > 0)
    }

    override fun localize() {
        encoders.forEach(Encoder::read)

        val newAngle = (((leftEncoder.currRead - rightEncoder.currRead) / config.TRACK_WIDTH) + startPose.heading).wrap

        val headingDelta = (leftEncoder.delta - rightEncoder.delta) / config.TRACK_WIDTH
        val perpPredicted = headingDelta * config.PERP_TRACKER
        val perpReal = perpEncoder.delta - perpPredicted

        accumulatedHeading += headingDelta
        accumulatedPerpPrediction += perpPredicted

        val deltaY = (leftEncoder.delta - rightEncoder.delta) / 2.0
        val pointIncrement = updatePoseWithDeltas(_position, leftEncoder.delta, rightEncoder.delta, perpReal, deltaY, headingDelta)

        _position = Pose(_position.point + pointIncrement, newAngle)
    }
}
