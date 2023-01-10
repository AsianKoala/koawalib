package com.asiankoala.koawalib.subsystem.drive

import com.asiankoala.koawalib.hardware.motor.KMotor
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.subsystem.KSubsystem
import kotlin.math.absoluteValue

open class KMecanumDrive(
    fl: KMotor,
    bl: KMotor,
    br: KMotor,
    fr: KMotor
) : KSubsystem() {
    val motors = listOf(fl, bl, br, fr)

    var powers = Pose()

    override fun periodic() {
        val wheels = mecKinematics(powers)
        val absMax = wheels.maxOf { it.absoluteValue }
        val scalar = if (absMax > 1.0) absMax else 1.0
        motors.forEachIndexed { i, it -> it.power = wheels[i] / scalar }
    }

    companion object {
        /**
         * Given a robot at (0,0) facing 0 degrees
         * +x power moves along +y axis, -x power moves along -y axis
         * +y power moves along +x axis, -y power moves along -x axis
         * +h power rotates counter clockwise (increasing angle),
         * -h power rotates clockwise (decreasing angle)
         */
        fun mecKinematics(vel: Pose) = listOf(
            vel.y + vel.x - vel.heading,
            vel.y - vel.x - vel.heading,
            vel.y + vel.x + vel.heading,
            vel.y - vel.x + vel.heading
        )
    }
}
