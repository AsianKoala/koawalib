package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.hardware.motor.KEncoder
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.Pose
import kotlin.math.absoluteValue

class KThreeWheelOdometry(
    private val leftEncoder: KEncoder,
    private val rightEncoder: KEncoder,
    private val auxEncoder: KEncoder,
    private val LEFT_OFFSET: Double,
    private val RIGHT_OFFSET: Double,
    private val PERP_TRACKER: Double,
    startPose: Pose,
) : Odometry(startPose) {
    private var encoders = listOf(leftEncoder, rightEncoder, auxEncoder)
    private var accumulatedAuxPrediction = 0.0
    private var accumulatedAux = 0.0
    private val radius2 = LEFT_OFFSET - RIGHT_OFFSET

    override fun updateTelemetry() {
        Logger.addTelemetryData("start pose", startPose)
        Logger.addTelemetryData("curr pose", pose)
        Logger.addTelemetryData("left encoder", leftEncoder.pos)
        Logger.addTelemetryData("right encoder", rightEncoder.pos)
        Logger.addTelemetryData("aux encoder", auxEncoder.pos)
        Logger.addTelemetryData("delta tracker", accumulatedAux - accumulatedAuxPrediction)
        Logger.addTelemetryData("radius", radius2)
    }

    override fun reset(p: Pose) {
        encoders.forEach(KEncoder::zero)
        pose = p
        startPose = p
    }

    override fun periodic() {
        encoders.forEach(KEncoder::update)
        val ldt = leftEncoder.delta
        val rdt = rightEncoder.delta
        val dtheta = (rightEncoder.delta - leftEncoder.delta) / radius2
        val dy = (LEFT_OFFSET * rdt + RIGHT_OFFSET * ldt) / radius2
        val predict = dtheta * PERP_TRACKER
        val dx = auxEncoder.delta - dtheta * PERP_TRACKER
        pose = exp(pose, Pose(dx, dy, dtheta))
        accumulatedAuxPrediction += predict.absoluteValue
        accumulatedAux += auxEncoder.delta.absoluteValue
    }
}
