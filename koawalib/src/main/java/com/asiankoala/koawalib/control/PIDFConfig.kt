package com.asiankoala.koawalib.control

import com.asiankoala.koawalib.control.feedforward.Feedforward

data class PIDFConfig(
    val kP: Double,
    val kI: Double,
    val kD: Double,

    val feedforward: Feedforward,

    val positionEpsilon: Double,
    val homePositionToDisable: Double,
    val ticksPerUnit: Double
)
