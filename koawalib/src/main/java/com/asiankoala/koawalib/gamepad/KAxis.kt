package com.asiankoala.koawalib.gamepad

import com.asiankoala.koawalib.util.KDouble
import com.asiankoala.koawalib.util.Periodic
import kotlin.math.absoluteValue

@Suppress("unused")
class KAxis(
    private val axis: () -> Double,
) : KDouble {
    override fun invokeDouble(): Double {
        return axis.invoke()
    }

    val inverted
        get() = KAxis { -axis.invoke() }
}
