package com.asiankoala.koawalib.subsystem.drive

import com.asiankoala.koawalib.hardware.motor.KMotor
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.subsystem.Subsystem
import kotlin.math.absoluteValue

open class KMecanumDrive(
    fl: KMotor,
    bl: KMotor,
    br: KMotor,
    fr: KMotor
) : Subsystem() {
    private val motors = listOf(fl, bl, br, fr)

    var powers = Pose()

    override fun periodic() {
        val wheels = mecKinematics(powers)
        val absMax = wheels.maxOf { it.absoluteValue }
        val scalar = if (absMax > 1.0) absMax else 1.0
        motors.forEachIndexed { i, it -> it.power = wheels[i] / scalar }
    }

    companion object {
        fun mecKinematics(drivePowers: Pose): List<Double> {
            val fl = drivePowers.y + drivePowers.x + drivePowers.heading
            val bl = drivePowers.y - drivePowers.x + drivePowers.heading
            val br = drivePowers.y + drivePowers.x - drivePowers.heading
            val fr = drivePowers.y - drivePowers.x - drivePowers.heading
            return listOf(fl, bl, br, fr)
        }
    }
}
