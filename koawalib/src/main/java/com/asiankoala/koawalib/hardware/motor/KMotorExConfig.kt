package com.asiankoala.koawalib.hardware.motor

import com.asiankoala.koawalib.control.FeedforwardConstants
import com.asiankoala.koawalib.control.MotorControlType
import com.asiankoala.koawalib.control.PIDConstants
import com.asiankoala.koawalib.subsystem.odometry.KEncoder

data class KMotorExConfig(
    val name: String,
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