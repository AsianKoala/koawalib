package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.hardware.motor.KEncoder
import com.asiankoala.koawalib.hardware.sensor.KIMU
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.Pose

/**
 * Standard three wheel localizer
 * Offets are assuming the robot is placed at (0,0) facing 0 degrees.
 * @param[parEnc] Parallel encoder
 * @param[perpEnc] Perp encoder
 * @param[PARALLEL_OFFSET] Y offset of the parallel odo pod from (0,0)
 * @param[PERP_OFFSET] X offset of the perp odo pod from (0,0)
 */
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
        Logger.put("start pose", startPose)
        Logger.put("curr pose", pose)
        Logger.put("parallel encoder", parEnc.pos)
        Logger.put("perp encoder", perpEnc.pos)
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
