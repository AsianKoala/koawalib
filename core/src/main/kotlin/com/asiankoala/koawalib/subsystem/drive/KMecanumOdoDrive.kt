package com.asiankoala.koawalib.subsystem.drive

import com.asiankoala.koawalib.hardware.motor.KMotor
import com.asiankoala.koawalib.subsystem.odometry.Odometry

open class KMecanumOdoDrive(
    fl: KMotor,
    bl: KMotor,
    br: KMotor,
    fr: KMotor,
    private val odometry: Odometry,
    private val shouldTelemetryOdo: Boolean
) : KMecanumDrive(fl, bl, br, fr) {
    val pose get() = odometry.pose
    val vel get() = odometry.vel

    protected fun updateOdo() {
        if (shouldTelemetryOdo) {
            odometry.updateTelemetry()
        }
    }

    override fun periodic() {
        super.periodic()
        updateOdo()
    }
}
