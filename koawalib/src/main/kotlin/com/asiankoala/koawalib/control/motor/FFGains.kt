package com.asiankoala.koawalib.control.motor

import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.cos
import com.asiankoala.koawalib.math.radians

data class FFGains(
    var kS: Double = 0.0,
    var kV: Double = 0.0,
    var kA: Double = 0.0,
    var kG: Double = 0.0,
    var kCos: Double? = null,
) {
    fun calc(targetX: Double): Double {
        val armFF = kCos?.times(targetX.radians.cos) ?: 0.0
        Logger.addTelemetryData("arm ff", armFF)
        return kG + armFF
    }
}
