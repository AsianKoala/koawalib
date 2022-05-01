package com.asiankoala.koawalib.rework

import com.asiankoala.koawalib.control.motion.MotionConstraints

class LiftComplexMotor(
    var kG: Double,
    settings: ComplexMotorSettings,
    kS: Double,
    kV: Double,
    kA: Double,
    constraints: MotionConstraints,
    allowedPositionError: Double,
    allowedVelocityError: Double = Double.POSITIVE_INFINITY,
    disabledPosition: Double? = null,
) : ComplexMotor(
    settings, kS, kV, kA, constraints, allowedPositionError, allowedVelocityError, disabledPosition
) {
    override val calculateFeedforward: Double
        get() = kG

    fun build(): LiftComplexMotor = this
}
