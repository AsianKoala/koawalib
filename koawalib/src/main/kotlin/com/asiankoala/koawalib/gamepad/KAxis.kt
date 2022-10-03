package com.asiankoala.koawalib.gamepad

import com.asiankoala.koawalib.util.internal.KDouble

class KAxis(
    private val axis: () -> Double
) : KDouble<KAxis> {
    override fun invokeDouble(): Double {
        return axis.invoke()
    }

    override fun inverted(): KAxis {
        return KAxis { axis.invoke() * -1.0 }
    }
}