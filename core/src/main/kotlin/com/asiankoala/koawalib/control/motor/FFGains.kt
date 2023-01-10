package com.asiankoala.koawalib.control.motor

import com.asiankoala.koawalib.math.radians
import kotlin.math.cos

data class FFGains @JvmOverloads constructor(
    val kS: Double = 0.0,
    val kV: Double = 0.0,
    val kA: Double = 0.0,
    val kG: Double = 0.0,
    val kCos: Double? = null,
) {
    fun calc(targetX: Double): Double {
        val armFF = kCos?.times(cos(targetX.radians)) ?: 0.0
        return kG + armFF
    }
}
