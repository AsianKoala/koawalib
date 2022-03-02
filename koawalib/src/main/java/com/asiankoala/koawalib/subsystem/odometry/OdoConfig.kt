package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.hardware.motor.KMotor

data class OdoConfig(
    val TICKS_PER_INCH: Double,
    var TURN_SCALAR: Double,
    var AUX_TRACKER: Double,
    val leftEncoder: KMotor,
    val rightEncoder: KMotor,
    val auxEncoder: KMotor,
    val LEFT_SCALAR: Double = 1.0,
    val RIGHT_SCALAR: Double = 1.0,
    val AUX_SCALAR: Double = 1.0,
    val VELOCITY_READ_TICKS: Int = 5
)
