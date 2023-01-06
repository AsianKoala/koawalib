package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.hardware.motor.KEncoder
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.Pose
import kotlin.math.absoluteValue

class KThreeWheelOdometry(
    private val leftEnc: KEncoder,
    private val rightEnc: KEncoder,
    private val perpEnc: KEncoder,
    private val LEFT_OFFSET: Double,
    private val RIGHT_OFFSET: Double,
    private val PERP_OFFSET: Double,
    startPose: Pose
) : Odometry(startPose) {
    private var encoders = listOf(leftEnc, rightEnc, perpEnc)
    private val radius2 = LEFT_OFFSET - RIGHT_OFFSET
    private var accumulatedLeftTheta = 0.0
    private var accumulatedRightTheta = 0.0
    private var accumulatedAuxDelta = 0.0

    override fun updateTelemetry() {
        Logger.addTelemetryData("start pose", startPose)
        Logger.addTelemetryData("curr pose", pose)
        Logger.addTelemetryData("left encoder", leftEnc.pos)
        Logger.addTelemetryData("right encoder", rightEnc.pos)
        Logger.addTelemetryData("perp encoder", perpEnc.pos)
        Logger.addTelemetryData("delta tracker", accumulatedAuxDelta)
        Logger.addTelemetryData("accumulated left theta", accumulatedLeftTheta)
        Logger.addTelemetryData("accumulated right theta", accumulatedRightTheta)
    }

    override fun reset(p: Pose) {
        encoders.forEach(KEncoder::zero)
        pose = p
        startPose = p
    }

    override fun periodic() {
        encoders.forEach(KEncoder::update)
        val ldt = leftEnc.delta
        val rdt = rightEnc.delta
        val dtheta = (ldt - rdt) / radius2
        val dy = (LEFT_OFFSET * rdt - RIGHT_OFFSET * ldt) / radius2
        val dx = perpEnc.delta - dtheta * PERP_OFFSET
        pose = exp(pose, Pose(dx, dy, dtheta))

        accumulatedLeftTheta += ldt
        accumulatedRightTheta += rdt
        accumulatedAuxDelta += dx.absoluteValue
    }
}
