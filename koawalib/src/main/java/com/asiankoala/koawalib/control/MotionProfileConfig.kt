package com.asiankoala.koawalib.control

data class MotionProfileConfig(
    val pidConfig: PIDFConfig,
    val maxVelocity: Double,
    val maxAcceleration: Double,
)
