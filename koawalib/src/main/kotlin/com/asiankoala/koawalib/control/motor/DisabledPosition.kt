package com.asiankoala.koawalib.control.motor

import com.acmerobotics.roadrunner.util.epsilonEquals
import kotlin.math.absoluteValue

class DisabledPosition(
    private val position: Double,
) {
    internal var enabled: Boolean = true
    fun shouldDisable(target: Double, current: Double, epsilon: Double): Boolean {
        return enabled && target epsilonEquals position && (current - position).absoluteValue < epsilon
    }

    companion object {
        val NONE = DisabledPosition(0.0).apply { enabled = false }
    }
}
