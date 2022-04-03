package com.asiankoala.koawalib.subsystem.old

import com.asiankoala.koawalib.hardware.motor.KMotor
import com.asiankoala.koawalib.subsystem.odometry.Encoder

/**
 * @see com.asiankoala.koawalib.hardware.motor.KMotorEx
 * @see com.asiankoala.koawalib.control.PIDExController
 * @see com.asiankoala.koawalib.control.MotionProfileController
 */
//@Deprecated("port of koawalib v0, do not use")
data class MotorSubsystemConfig(
    val motor: KMotor,
    val encoder: Encoder?,
    val controlType: MotorControlType,

    val pid: PIDConstants,
    val ff: FeedforwardConstants,

    val positionEpsilon: Double,
    val homePositionToDisable: Double = Double.NaN,
    val maxVelocity: Double = 0.0,
    val maxAcceleration: Double = 0.0,
)