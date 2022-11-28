package com.asiankoala.koawalib.gamepad

class KAxis(
    private val axis: () -> Double
) : () -> Double {
    override fun invoke() = axis.invoke()

    fun inverted(): KAxis {
        return KAxis { axis.invoke() * -1.0 }
    }
}
