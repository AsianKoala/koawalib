package com.asiankoala.koawalib.control.motor

import com.asiankoala.koawalib.math.epsilonEquals
import kotlin.math.absoluteValue

internal class DisabledPosition(
    private val position: Double? = null,
) {
    fun shouldDisable(target: Double, current: Double, epsilon: Double): Boolean {
        return position?.let {
            target epsilonEquals position && (current - position).absoluteValue < epsilon
        } ?: false
    }
}
