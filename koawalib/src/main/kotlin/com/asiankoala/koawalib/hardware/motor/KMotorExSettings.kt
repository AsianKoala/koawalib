package com.asiankoala.koawalib.hardware.motor

import com.asiankoala.koawalib.control.motion.MotionConstraints
import com.asiankoala.koawalib.math.assertPositive

data class KMotorExSettings(
    var name: String,
    val ticksPerUnit: Double,
    val isRevEncoder: Boolean,
    var allowedPositionError: Double,
    val _kP: Double,
    val _kI: Double,
    val _kD: Double,
    var kS: Double,
    var kV: Double,
    var kA: Double,
    var kG: Double,
    var kCos: Double,
    var constraints: MotionConstraints? = null,
    var allowedVelocityError: Double = Double.POSITIVE_INFINITY,
    var disabledPosition: Double? = null,
    var isCompletelyDisabled: Boolean = false,
    var isPIDEnabled: Boolean = false,
    var isUsingVoltageFF: Boolean = false,
    var isFFEnabled: Boolean = false,
    var isMotionProfiled: Boolean = true
)
