package com.asiankoala.koawalib.subsystem.drive

import com.asiankoala.koawalib.hardware.motor.KMotor
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.subsystem.DeviceSubsystem
import kotlin.math.absoluteValue

open class KMecanumDrive(
    fl: KMotor,
    bl: KMotor,
    br: KMotor,
    fr: KMotor
) : DeviceSubsystem() {
    private val motors = listOf(fl, bl, fr, br)

    var powers = Pose()

    private fun processPowers() {
        val fl = powers.y + powers.x - powers.heading
        val bl = powers.y - powers.x - powers.heading
        val fr = powers.y - powers.x + powers.heading
        val br = powers.y + powers.x + powers.heading

        val wheels = listOf(fl, bl, fr, br)
        val absMax = wheels.maxOf { it.absoluteValue }
        val scalar = if (absMax > 1.0) absMax else 1.0
        motors.forEachIndexed { i, it -> it.setSpeed(wheels[i] / scalar) }
    }

    override fun periodic() {
        processPowers()
    }
}
