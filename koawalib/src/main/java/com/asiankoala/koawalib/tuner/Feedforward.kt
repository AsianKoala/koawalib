package com.asiankoala.koawalib.tuner

import kotlin.math.sign

data class Feedforward(
    var kS: Double,
    var kV: Double,
    var kA: Double
) {
    fun calculateMotorFeedforward(v: Double, a: Double): Double {
        return kS * v.sign + kV * v + kA * a
    }
}

class meow() {
    fun b() {
        val t = Feedforward(0.0, 0.0, 0.0)
        t.component1().javaClass.simpleName
    }
}