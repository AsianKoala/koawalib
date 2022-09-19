package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.hardware.motor.KEncoder
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.math.degrees
import kotlin.math.absoluteValue

class KThreeWheelOdometry(
    private val leftEncoder: KEncoder,
    private val rightEncoder: KEncoder,
    private val auxEncoder: KEncoder,
    private val TRACK_WIDTH: Double,
    private val PERP_TRACKER: Double,
) : Odometry() {
    private var encoders = listOf(leftEncoder, rightEncoder, auxEncoder)
    private var accumulatedHeading = 0.0
    private var accumulatedAuxPrediction = 0.0
    private var accumulatedAux = 0.0

    override fun updateTelemetry() {
        Logger.addTelemetryData("start pose", startPose)
        Logger.addTelemetryData("curr pose", pose)
        Logger.addTelemetryData("left encoder", leftEncoder.pos)
        Logger.addTelemetryData("right encoder", rightEncoder.pos)
        Logger.addTelemetryData("aux encoder", auxEncoder.pos)
        Logger.addTelemetryData("accumulated heading", accumulatedHeading.degrees)
        Logger.addTelemetryData("delta tracker", accumulatedAux - accumulatedAuxPrediction)
    }

    override fun reset() {
        encoders.forEach(KEncoder::zero)
    }

    override fun periodic() {
        encoders.forEach(KEncoder::update)

        val newAngle = (((rightEncoder.pos - leftEncoder.pos) / TRACK_WIDTH) + startPose.heading).angleWrap

        val headingDelta = (rightEncoder.delta - leftEncoder.delta) / TRACK_WIDTH
        val auxPredicted = headingDelta * PERP_TRACKER
        val auxDelta = auxEncoder.delta - auxPredicted

        accumulatedHeading += headingDelta.absoluteValue
        accumulatedAuxPrediction += auxPredicted.absoluteValue
        accumulatedAux += auxEncoder.delta.absoluteValue

        val deltaY = (leftEncoder.delta - rightEncoder.delta) / 2.0
        val pointIncrement = updatePoseWithDeltas(pose, leftEncoder.delta, rightEncoder.delta, auxDelta, deltaY, headingDelta)
        pose = Pose(pose.vec + pointIncrement, newAngle)
    }
}
