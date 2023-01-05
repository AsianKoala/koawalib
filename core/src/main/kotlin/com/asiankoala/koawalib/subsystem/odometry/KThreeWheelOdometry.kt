package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.hardware.motor.KEncoder
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import kotlin.math.absoluteValue

class KThreeWheelOdometry(
    private val leftEncoder: KEncoder,
    private val rightEncoder: KEncoder,
    private val perpEncoder: KEncoder,
    private val LEFT_OFFSET: Double,
    private val RIGHT_OFFSET: Double,
    private val PERP_OFFSET: Double,
    startPose: Pose
) : Odometry(startPose) {
    private var encoders = listOf(leftEncoder, rightEncoder, perpEncoder)
    private val radiusDiff = LEFT_OFFSET - RIGHT_OFFSET
    private val offsets = Vector(LEFT_OFFSET, RIGHT_OFFSET)
    private var accumulatedLeftTheta = 0.0
    private var accumulatedRightTheta = 0.0
    private var accumulatedAuxDelta = 0.0

    override fun updateTelemetry() {
        Logger.addTelemetryData("start pose", startPose)
        Logger.addTelemetryData("curr pose", pose)
        Logger.addTelemetryData("left encoder", leftEncoder.pos)
        Logger.addTelemetryData("right encoder", rightEncoder.pos)
        Logger.addTelemetryData("perp encoder", perpEncoder.pos)
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

        val ldt = leftEncoder.delta / LEFT_OFFSET
        val rdt = rightEncoder.delta / RIGHT_OFFSET
        val dtheta = ldt - rdt
        val deltas = Vector(leftEncoder.delta, rightEncoder.delta)
        val dy = (offsets cross deltas) / radiusDiff
        val dx = perpEncoder.delta - dtheta * PERP_OFFSET
        pose = exp(pose, Pose(dx, dy, dtheta))

        accumulatedLeftTheta += ldt
        accumulatedRightTheta += rdt
        accumulatedAuxDelta += dx.absoluteValue
    }
}
