package com.asiankoala.koawalib.control.feedforward

class DisableFeedforward() : Feedforward {
    override val kStatic: Double
        get() = 0.0

    override fun getFeedforward(x: Double, v: Double, a: Double): Double {
        return 0.0
    }
}
