package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.math.*
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.util.Logger

class ThreeWheelOdometry(
    private val leftEncoder: Encoder,
    private val rightEncoder: Encoder,
    private val perpEncoder: Encoder,
    private val TRACK_WIDTH: Double,
    private val PERP_TRACKER: Double
) : Odometry() {
    private var encoders = listOf(leftEncoder, rightEncoder, perpEncoder)
    private var accumulatedHeading = 0.0
    private var accumulatedPerpPrediction = 0.0

    override fun updateTelemetry() {
        Logger.addTelemetryData("start pose", startPose)
        Logger.addTelemetryData("curr pose", position)
        Logger.addTelemetryData("left encoder", leftEncoder.position)
        Logger.addTelemetryData("right encoder", rightEncoder.position)
        Logger.addTelemetryData("perp encoder", perpEncoder.position)
        Logger.addTelemetryData("accumulated heading", accumulatedHeading.degrees)

        val accumPerp = perpEncoder.position
        val perpPredictDelta = accumPerp - accumulatedPerpPrediction
        Logger.addTelemetryData("accumulated perp", accumPerp)
        Logger.addTelemetryData("accumulated perp prediction", accumulatedPerpPrediction)
        Logger.addTelemetryData("accum perp - tracker", perpPredictDelta)
        Logger.addTelemetryData("should increase perp tracker", perpPredictDelta > 0)
    }

    override fun periodic() {
        encoders.forEach(Encoder::update)

        val newAngle = (((leftEncoder.position - rightEncoder.position) / TRACK_WIDTH) + startPose.heading).wrap

        val headingDelta = (leftEncoder.delta - rightEncoder.delta) / TRACK_WIDTH
        val perpPredicted = headingDelta * PERP_TRACKER
        val perpReal = perpEncoder.delta - perpPredicted

        accumulatedHeading += headingDelta
        accumulatedPerpPrediction += perpPredicted

        val deltaY = (leftEncoder.delta - rightEncoder.delta) / 2.0
        val pointIncrement = updatePoseWithDeltas(_position, leftEncoder.delta, rightEncoder.delta, perpReal, deltaY, headingDelta)

        _position = Pose(_position.point + pointIncrement, newAngle)
    }
}
