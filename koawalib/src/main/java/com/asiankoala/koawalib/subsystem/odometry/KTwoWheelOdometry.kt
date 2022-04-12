package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.hardware.sensor.KIMU
import com.asiankoala.koawalib.math.*
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.util.Logger

class KTwoWheelOdometry(
    private val imu: KIMU,
    private val leftEncoder: KEncoder,
    private val auxEncoder: KEncoder,
    private val TRACK_WIDTH: Double,
    private val PERP_TRACKER: Double,
) : Odometry() {
    private val encoders = listOf(leftEncoder, auxEncoder)
    private var accumulatedAuxPrediction = 0.0
    private var accumRWheel = 0.0
    private var lastAngle = Double.NaN

    private fun getHeading(): Double {
        return (imu.heading + startPose.heading).angleWrap
    }

    override fun reset() {
        lastAngle = getHeading()
        encoders.forEach(KEncoder::zero)
        shouldReset = false
    }

    override fun updateTelemetry() {
        Logger.addTelemetryData("start pose", startPose)
        Logger.addTelemetryData("curr pose", pose)
        Logger.addTelemetryData("left encoder", leftEncoder.position)
        Logger.addTelemetryData("aux encoder", auxEncoder.position)

        val accumAuxScale = auxEncoder.position
        val auxTrackDiff = accumAuxScale - accumulatedAuxPrediction
        Logger.addTelemetryData("accumulated aux", accumAuxScale)
        Logger.addTelemetryData("accumulated aux prediction", accumulatedAuxPrediction)
        Logger.addTelemetryData("accum aux - tracker", auxTrackDiff)
    }

    override fun periodic() {
        encoders.forEach(KEncoder::update)
        imu.update()

        if(shouldReset) {
            reset()
        }

        val newAngle = getHeading()
        val angleIncrement = (newAngle - lastAngle).angleWrap
        val auxPrediction = angleIncrement * PERP_TRACKER
        val rX = auxEncoder.delta - auxPrediction

        accumulatedAuxPrediction += auxPrediction

        val rWheelDelta = -(angleIncrement * TRACK_WIDTH - leftEncoder.delta)
        accumRWheel += rWheelDelta
        val deltaY = (leftEncoder.delta + rWheelDelta) / 2.0

        val pointIncrement = updatePoseWithDeltas(pose, leftEncoder.delta, rWheelDelta, rX, deltaY, angleIncrement)

        pose = Pose(pose.vec + pointIncrement, newAngle)
        lastAngle = newAngle
    }
}
