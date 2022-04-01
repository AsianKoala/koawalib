package com.asiankoala.koawalib.subsystem.old

import com.asiankoala.koawalib.control.PIDFConfig
import com.asiankoala.koawalib.control.feedforward.Feedforward
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
    
    val kP: Double = 0.0,
    val kI: Double = 0.0,
    val kD: Double = 0.0,
    val kV: Double = 0.0,
    val kA: Double = 0.0,
    val kStatic: Double = 0.0,
    val kF: (Double, Double?) -> Double = { x,v -> 0.0 },

    val positionEpsilon: Double,
    val maxVelocity: Double = 0.0,
    val maxAcceleration: Double = 0.0,
    val homePositionToDisable: Double = Double.NaN,
)