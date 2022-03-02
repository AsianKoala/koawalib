package com.asiankoala.koawalib.gamepad

import com.asiankoala.koawalib.gamepad.functionality.Input
import com.asiankoala.koawalib.util.KDouble
import com.asiankoala.koawalib.util.Periodic
import kotlin.math.absoluteValue

class GamepadAxis(
    private val axis: () -> Double,
) : ButtonBase(), Input<GamepadAxis>, KDouble, Periodic {
    private var triggerThreshold = DEFAULT_TRIGGER_THRESHOLD

    override fun instance(): GamepadAxis {
        return this
    }

    fun setTriggerThreshold(threshold: Double): GamepadAxis {
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
