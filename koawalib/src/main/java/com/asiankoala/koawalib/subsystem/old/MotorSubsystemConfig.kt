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
    val gearRatio: Double,
    val maxVelocity: Double,
    val maxAcceleration: Double,

    val kP: Double,
    val kI: Double,
    val kD: Double,
    val kV: Double,
    val kA: Double,
    val kStatic: Double,
    val kF: (Double, Double?) -> Double,
    val positionEpsilon: Double,
    val homePositionToDisable: Double,
    val ticksPerUnit: Double
)