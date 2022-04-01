package com.asiankoala.koawalib.roadrunner.drive

import com.asiankoala.koawalib.math.radians
import com.qualcomm.robotcore.hardware.PIDFCoefficients

data class DriveConstants(
    @JvmField var TICKS_PER_REV: Double = 1.0,
    @JvmField var MAX_RPM: Double = 1.0,
    @JvmField var WHEEL_RADIUS: Double = 2.0,
    @JvmField var GEAR_RATIO: Double = 1.0,
    @JvmField var TRACK_WIDTH: Double = 1.0,
    @JvmField var kV: Double = 1.0 / rpmToVelocity(MAX_RPM, GEAR_RATIO, WHEEL_RADIUS),
    @JvmField var ka: Double = 0.0,
    @JvmField var kStatic: Double = 0.0,
    @JvmField var MAX_VEL: Double = 30.0,
    @JvmField var MAX_ACCEL: Double = 30.0,
    @JvmField var MAX_ANG_VEL: Double = 60.0.radians,
    @JvmField var MAX_ANG_ACCEL: Double = 60.0.radians
) {

    companion object {
        fun encoderTicksToInches(ticks: Double, WHEEL_RADIUS: Double, GEAR_RATIO: Double, TICKS_PER_REV: Double): Double {
            return WHEEL_RADIUS * 2 * Math.PI * GEAR_RATIO * ticks / TICKS_PER_REV
        }

        fun rpmToVelocity(rpm: Double, GEAR_RATIO: Double, WHEEL_RADIUS: Double): Double {
            return rpm * GEAR_RATIO * 2 * Math.PI * WHEEL_RADIUS / 60.0
        }
    }
}