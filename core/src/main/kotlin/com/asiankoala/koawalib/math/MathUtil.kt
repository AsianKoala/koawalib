package com.asiankoala.koawalib.math

import kotlin.math.*

const val EPSILON = 1e-6
const val TAU = 2 * PI

infix fun Double.epsilonEquals(other: Double) = (this - other).absoluteValue < EPSILON
infix fun Double.epsilonNotEqual(other: Double) = (this - other).absoluteValue > EPSILON

val Double.radians get() = Math.toRadians(this)
val Double.degrees get() = Math.toDegrees(this)

fun clamp(x: Double, a: Double, b: Double): Double {
    return when {
        x < a -> a
        x > b -> b
        else -> x
    }
}

val Int.d get() = this.toDouble()
val Float.d get() = this.toDouble()

val Double.angleWrap: Double
    get() {
        var wrapped = this % TAU
        wrapped = (wrapped + TAU) % TAU
        return wrapped
    }