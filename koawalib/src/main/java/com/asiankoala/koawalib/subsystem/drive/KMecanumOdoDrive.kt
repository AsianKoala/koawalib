package com.asiankoala.koawalib.subsystem.drive

import com.acmerobotics.dashboard.config.Config
import com.asiankoala.koawalib.hardware.motor.KMotor
import com.asiankoala.koawalib.subsystem.odometry.OdoConfig
import com.asiankoala.koawalib.subsystem.odometry.Odometry

@Config
open class KMecanumOdoDrive(
    fl: KMotor,
    bl: KMotor,
    fr: KMotor,
    br: KMotor,
    odoConfig: OdoConfig
) : KMecanumDrive(fl, bl, fr, br) {

    private val odometry = Odometry(odoConfig)

    val position get() = odometry.position
    val velocity get() = odometry.velocity

    override fun periodic() {
        super.periodic()
        odometry.localize()
    }
}
