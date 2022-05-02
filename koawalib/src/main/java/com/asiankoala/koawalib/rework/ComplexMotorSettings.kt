package com.asiankoala.koawalib.rework

import com.asiankoala.koawalib.control.motion.MotionConstraints
import com.asiankoala.koawalib.math.assertPositive

data class ComplexMotorSettings(
    var name: String,
    val ticksPerUnit: Double,
    val isRevEncoder: Boolean,
    val _kP: Double,
    val _kI: Double,
    val _kD: Double,
    var kS: Double,
    var kV: Double,
    var kA: Double,
    var kG: Double,
    var kCos: Double,
    var constraints: MotionConstraints,
    var allowedPositionError: Double,
    var allowedVelocityError: Double = Double.POSITIVE_INFINITY,
    var disabledPosition: Double? = null,
) {
    var isCompletelyDisabled = false
    var isPIDEnabled = false
    var isUsingVoltageFF = false
    var isFFEnabled = false

    init {
        assertPositive(ticksPerUnit)
        assertPositive(_kP)
        assertPositive(_kI)
        assertPositive(_kD)
        assertPositive(kS)
        assertPositive(kV)
        assertPositive(kA)
        assertPositive(constraints.vMax)
        assertPositive(constraints.aMax)
        assertPositive(constraints.dMax)
        assertPositive(allowedPositionError)
        assertPositive((allowedVelocityError))
    }
}
