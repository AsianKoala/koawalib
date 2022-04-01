package com.asiankoala.koawalib.roadrunner.drive

import com.asiankoala.koawalib.math.radians
import com.qualcomm.robotcore.hardware.PIDFCoefficients

data class DriveConstants(
    val TICKS_PER_REV: Double = 1.0,
    val MAX_RPM: Double = 1.0,
    val RUN_USING_ENCODER: Boolean = false,
    val MOTOR_VELO_PID: PIDFCoefficients = PIDFCoefficients(0.0, 0.0, 0.0, getMotorVelocityF(MAX_RPM / 60 * TICKS_PER_REV)),
    val WHEEL_RADIUS: Double = 2.0,
    val GEAR_RATIO: Double = 1.0,
    val TRACK_WIDTH: Double = 1.0,
    val kV: Double = 1.0 / rpmToVelocity(MAX_RPM, GEAR_RATIO, WHEEL_RADIUS),
    val ka: Double = 0.0,
    val kStatic: Double = 0.0,
    val MAX_VEL: Double = 30.0,
    val MAX_ACCEL: Double = 30.0,
    val MAX_ANG_VEL: Double = 60.0.radians,
    val MAX_AANG_ACCEL: Double = 60.0.radians
) {

    companion object {
        fun encoderTicksToInches(ticks: Double, WHEEL_RADIUS: Double, GEAR_RATIO: Double, TICKS_PER_REV: Double): Double {
            return WHEEL_RADIUS * 2 * Math.PI * GEAR_RATIO * ticks / TICKS_PER_REV
        }

        fun rpmToVelocity(rpm: Double, GEAR_RATIO: Double, WHEEL_RADIUS: Double): Double {
            return rpm * GEAR_RATIO * 2 * Math.PI * WHEEL_RADIUS / 60.0
        }

        fun getMotorVelocityF(ticksPerSecond: Double): Double {
            // see https://docs.google.com/document/d/1tyWrXDfMidwYyP_5H4mZyVgaEswhOC35gvdmP-V-5hA/edit#heading=h.61g9ixenznbx
            return 32767 / ticksPerSecond
        }
    }
}