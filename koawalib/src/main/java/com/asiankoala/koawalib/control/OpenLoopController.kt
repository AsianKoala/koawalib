package com.asiankoala.koawalib.control

class OpenLoopController : Controller() {
    fun setDirectOutput(power: Double) {
        output = power
    }

    override fun process(): Double {
        return Double.NaN
    }
}
