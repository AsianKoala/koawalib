package com.asiankoala.koawalib.control.feedforward

interface Feedforward {
    fun getFeedforward(x: Double, v: Double, a: Double): Double
    val kStatic: Double
}
