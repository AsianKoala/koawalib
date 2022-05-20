package com.asiankoala.koawalib.control.motor

import com.acmerobotics.roadrunner.util.epsilonEquals
import kotlin.math.absoluteValue

class DisabledPosition(
    private val position: Double,
    private val epsilon: Double,
    private val enabled: Boolean = true
) {
    fun shouldDisable(target: Double, current: Double): Boolean {
        return enabled && target epsilonEquals position && (current - position).absoluteValue < epsilon
    }

    companion object {
        val NONE = DisabledPosition(0.0, 0.0, false)
    }
}