package com.asiankoala.koawalib.control.motor

import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.cos
import com.asiankoala.koawalib.math.radians

data class FFGains(
    val kS: Double = 0.0,
    val kV: Double = 0.0,
    val kA: Double = 0.0,
    val kG: Double = 0.0,
    val kCos: Double? = null,
) {
    fun calc(targetX: Double): Double {
        val armFF = kCos?.times(targetX.radians.cos) ?: 0.0
        return kG + armFF
    }
}
