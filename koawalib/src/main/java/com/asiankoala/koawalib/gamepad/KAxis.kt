package com.asiankoala.koawalib.gamepad

import com.asiankoala.koawalib.gamepad.functionality.CommandButton
import com.asiankoala.koawalib.util.KDouble
import com.asiankoala.koawalib.util.Periodic
import kotlin.math.absoluteValue

@Suppress("unused")
class KAxis(
    private val axis: () -> Double,
) : CommandButton(), KDouble, Periodic {
    private var triggerThreshold = DEFAULT_TRIGGER_THRESHOLD

    fun setTriggerThreshold(threshold: Double): KAxis {
        triggerThreshold = threshold
        return this
    }

    override fun invokeDouble(): Double {
        return axis.invoke()
    }

    override fun invokeBoolean(): Boolean {
        return axis.invoke().absoluteValue > triggerThreshold
    }

    companion object {
        const val DEFAULT_TRIGGER_THRESHOLD = 0.3
    }
}
