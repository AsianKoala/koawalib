package com.asiankoala.koawalib.subsystem.intake

data class IntakeConfig(
    val ON_POWER: Double = 1.0,
    val OFF_POWER: Double = 0.0,
    val REVERSE_POWER: Double = -ON_POWER
)