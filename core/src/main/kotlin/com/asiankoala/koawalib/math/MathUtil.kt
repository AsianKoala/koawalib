package com.asiankoala.koawalib.math

import kotlin.math.*

const val EPSILON = 1e-6
const val TAU = 2 * PI

infix fun Double.epsilonEquals(other: Double) = (this - other).absoluteValue < EPSILON
infix fun Double.epsilonNotEqual(other: Double) = (this - other).absoluteValue > EPSILON

val Double.radians get() = Math.toRadians(this)
val Double.degrees get() = Math.toDegrees(this)

/**
 * Clamps an input value within [a,b]
 * @param[x] input value
 * @param[a] lower bound
 * @param[b] upper bound
 */
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
        var field = this
        while (field < -PI) field += TAU
        while (field > PI) field -= TAU
        return field
    }

fun nonZeroSign(x: Double) = if (x >= 0.0) 1.0 else -1.0

fun lineCircleIntersection(
    center: Vector,
    startVector: Vector,
    endVector: Vector,
    r: Double
): Vector {
    val start = startVector - center
    val end = endVector - center
    val deltas = end - start
    val dr = deltas.norm
    val D = start cross end
    val discriminant = r * r * dr * dr - D * D

    // discriminant = 0 for 1 intersection, >0 for 2
    val intersections = ArrayList<Vector>()
    val div = deltas.norm.pow(2)
    val left = Vector(D * deltas.y, -D * deltas.x) / div
    val right = Vector(
        nonZeroSign(deltas.y) * deltas.x * sqrt(discriminant),
        deltas.y.absoluteValue * sqrt(discriminant)
    ) / div
    if (discriminant == 0.0) {
        intersections.add(left)
    } else {
        intersections.add(left + right)
        intersections.add(left - right)
    }
    return intersections.map { it + center }.minByOrNull { it.dist(endVector) }!!
}
