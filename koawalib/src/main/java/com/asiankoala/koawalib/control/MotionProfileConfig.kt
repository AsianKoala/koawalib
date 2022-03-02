package com.asiankoala.koawalib.control

data class MotionProfileConfig(
    val pidConfig: PIDFConfig = PIDFConfig(),
    val maxVelocity: Double = 0.0,
    val maxAcceleration: Double = 0.0,
    val maxJerk: Double = 0.0
)
