package com.asiankoala.koawalib.roadrunner.drive

import com.acmerobotics.roadrunner.drive.MecanumDrive

class KMecanumDriveRR(
    private val constant: Double = 
) : MecanumDrive() {
    override val rawExternalHeading: Double
        get() = TODO("Not yet implemented")

    override fun getWheelPositions(): List<Double> {
        TODO("Not yet implemented")
    }

    override fun setMotorPowers(
        frontLeft: Double,
        rearLeft: Double,
        rearRight: Double,
        frontRight: Double
    ) {
        TODO("Not yet implemented")
    }
}