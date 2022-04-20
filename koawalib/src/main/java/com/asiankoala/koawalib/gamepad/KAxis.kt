package com.asiankoala.koawalib.gamepad

import com.asiankoala.koawalib.gamepad.functionality.Button
import com.asiankoala.koawalib.util.KDouble
import com.asiankoala.koawalib.util.Periodic
import kotlin.math.absoluteValue

@Suppress("unused")
class KAxis(
    private val axis: () -> Double,
) : KButton({ axis.invoke().absoluteValue > DEFAULT_TRIGGER_THRESHOLD}), KDouble {
    override fun invokeDouble(): Double {
        return axis.invoke()
    }

    val inverted
        get() = KAxis { -axis.invoke() }

    companion object {
        var DEFAULT_TRIGGER_THRESHOLD = 0.3
    }
}
