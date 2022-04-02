package com.asiankoala.koawalib.subsystem.drive

import com.acmerobotics.dashboard.config.Config
import com.asiankoala.koawalib.hardware.motor.KMotor
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.subsystem.odometry.Odometry
import com.asiankoala.koawalib.util.Logger

@Config
open class KMecanumOdoDrive(
    fl: KMotor,
    bl: KMotor,
    fr: KMotor,
    br: KMotor,
    private val odometry: Odometry,
    private val shouldTelemetryOdo: Boolean
) : KMecanumDrive(fl, bl, fr, br) {

    val position get() = odometry.position
    val velocity get() = odometry.velocity

    fun setStartPose(pose: Pose) {
        odometry.startPose = pose
        Logger.logInfo("attempted to set start pose to $pose")
    }

    override fun periodic() {
        super.periodic()
//        odometry.localize()

        // odometry is updated by commandscheduler now

        if (shouldTelemetryOdo) {
            odometry.updateTelemetry()
        }
    }
}
