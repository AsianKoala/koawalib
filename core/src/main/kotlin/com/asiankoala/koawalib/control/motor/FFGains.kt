package com.asiankoala.koawalib.control.motor

data class FFGains @JvmOverloads constructor(
    val kS: Double = 0.0,
    val kV: Double = 0.0,
    val kA: Double = 0.0,
    val kG: Double = 0.0,
    val kCos: Double? = null,
)
