package com.asiankoala.koawalib.hardware.motor

import kotlin.math.sign

data class FFSettings(
    var kS: Double = 0.0,
    var kV: Double = 0.0,
    var kA: Double = 0.0,
    var kG: Double = 0.0,
    var kCos: Double? = null,
) {
    fun calculate(
        v: Double,
        a: Double,
        x: Double,
        ) {
        return kS * v.sign +
                kV * v +
                kA * a +

    }
}
