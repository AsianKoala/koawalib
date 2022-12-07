package com.asiankoala.koawalib.control.controller

data class PIDGains(
    @JvmField var kP: Double = 0.0,
    @JvmField var kI: Double = 0.0,
    @JvmField var kD: Double = 0.0,
)
