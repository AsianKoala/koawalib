package com.asiankoala.koawalib.hardware.motor

import com.asiankoala.koawalib.control.FeedforwardConstants
import com.asiankoala.koawalib.control.MotorControlType
import com.asiankoala.koawalib.control.PIDConstants

/**
 * Config needed for creating a
 */
data class KMotorExConfig(
    val name: String,
    val ticksPerUnit: Double,
    val isRevEncoder: Boolean = false,
    val controlType: MotorControlType,

    val pid: PIDConstants,
    val ff: FeedforwardConstants,

    val positionEpsilon: Double,
    val homePositionToDisable: Double = Double.NaN,
    val lowerBound: Double = Double.NaN,
    val upperBound: Double = Double.NaN,
    val maxVelocity: Double = 0.0,
    val maxAcceleration: Double = 0.0,
)
