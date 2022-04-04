package com.asiankoala.koawalib.subsystem.old

import com.asiankoala.koawalib.hardware.motor.KMotor
import com.asiankoala.koawalib.subsystem.odometry.KEncoder

/**
 * @see com.asiankoala.koawalib.hardware.motor.KMotorEx
 * @see com.asiankoala.koawalib.control.PIDExController
 * @see com.asiankoala.koawalib.control.MotionProfileController
 */
//@Deprecated("port of koawalib v0, do not use")
data class MotorSubsystemConfig(
    val motor: KMotor,
    val encoder: KEncoder?,
    val controlType: MotorControlType,

    val pid: PIDConstants = PIDConstants(),
    val ff: FeedforwardConstants = FeedforwardConstants(),

    val positionEpsilon: Double,
    val homePositionToDisable: Double = Double.NaN,
    val lowerBound: Double = Double.NaN,
    val upperBound: Double = Double.NaN,
    val maxVelocity: Double = 0.0,
    val maxAcceleration: Double = 0.0,
)