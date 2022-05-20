package com.asiankoala.koawalib.control.motor

import com.asiankoala.koawalib.math.cos

data class FFGains(
    var kS: Double = 0.0,
    var kV: Double = 0.0,
    var kA: Double = 0.0,
    var kG: Double = 0.0,
    var kCos: Double? = null,
) {
    fun calc(pos: Double): Double {
        return kG + (kCos?.times(pos.cos) ?: 0.0)
    }
}
