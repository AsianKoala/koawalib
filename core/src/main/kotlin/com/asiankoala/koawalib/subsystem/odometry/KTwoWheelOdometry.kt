package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.hardware.motor.KEncoder
import com.asiankoala.koawalib.hardware.sensor.KIMU
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.Pose

// TODO: find what was causing so much drift
class KTwoWheelOdometry(
    private val parEnc: KEncoder,
    private val perpEnc: KEncoder,
    private val imu: KIMU,
    private val PARALLEL_OFFSET: Double,
    private val PERP_OFFSET: Double,
    startPose: Pose,
) : Odometry(startPose) {
    private val encoders = listOf(parEnc, perpEnc)
    override fun updateTelemetry() {
        Logger.addTelemetryData("start pose", startPose)
        Logger.addTelemetryData("curr pose", pose)
        Logger.addTelemetryData("parallel encoder", parEnc.pos)
        Logger.addTelemetryData("perp encoder", perpEnc.pos)
    }

    override fun reset(p: Pose) {
        encoders.forEach(KEncoder::zero)
        pose = p
    }

    override fun periodic() {
        encoders.forEach(KEncoder::update)
        val parDt = parEnc.delta
        val perpDt = perpEnc.delta
        val dtheta = imu.headingDelta
        val dy = parDt - PARALLEL_OFFSET * dtheta
        val dx = perpDt - PERP_OFFSET * dtheta
        pose = exp(pose, Pose(dx, dy, dtheta))
    }
}
