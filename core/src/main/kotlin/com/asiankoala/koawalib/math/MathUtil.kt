package com.asiankoala.koawalib.math

import kotlin.math.*

const val EPSILON = 1e-6
const val TAU = 2 * PI
const val VOLTAGE_CONSTANT = 12.0

infix fun Double.epsilonEquals(other: Double) = (this - other).absoluteValue < EPSILON
infix fun Double.epsilonNotEqual(other: Double) = (this - other).absoluteValue > EPSILON

val Double.radians get() = Math.toRadians(this)
val Double.degrees get() = Math.toDegrees(this)

fun absMax(a: Double, b: Double): Double {
    return if (a.absoluteValue >= b.absoluteValue) a else b
}

fun absMin(a: Double, b: Double): Double {
    return if (a.absoluteValue <= b.absoluteValue) a else b
}

fun clamp(x: Double, a: Double, b: Double): Double {
    if (x < a) {
        return a
    } else if (x > b) {
        return b
    }

    return x
}

fun assertPositive(value: Double) {
    if (value.sign < 0.0) throw Exception("value $value must be positive")
}

fun cubicScaling(k: Double, x: Double): Double {
    return (1 - k) * x + k * x * x * x
}

fun inputModulus(input: Double, minimumInput: Double, maximumInput: Double): Double {
    var inp = input
    val modulus = maximumInput - minimumInput

    // Wrap input if it's above the maximum input
    val numMax = ((inp - minimumInput) / modulus).toInt()
    inp -= numMax * modulus

    // Wrap input if it's below the minimum input
    val numMin = ((inp - maximumInput) / modulus).toInt()
    inp -= numMin * modulus
    return inp
}

fun stupidSign(a: Double): Int = if (a > 0) 1 else -1

val Double.sin get() = sin(this)
val Double.cos get() = cos(this)

val Int.d get() = this.toDouble()
val Float.d get() = this.toDouble()
val Long.d get() = this.toDouble()

val Double.angleWrap: Double
    get() {
        var wrapped = this
        while (wrapped > PI) wrapped -= TAU
        while (wrapped < -PI) wrapped += TAU
        return wrapped
    }

fun project(v: Vector, onto: Vector): Vector {
    return onto * ((v dot onto) / (onto dot onto))
}

fun extendLine(start: Vector, end: Vector, d: Double): Vector {
    return end + Vector.fromPolar(d, (end - start).angle)
}

/**
 * @param center          center point of circle
 * @param startVector start point of the line segment
 * @param endVector   end point of the line segment
 * @param radius     radius of the circle
 * @return intersection point closest to endPoint
 * @see [https://mathworld.wolfram.com/Circle-LineIntersection.html](https://mathworld.wolfram.com/Circle-LineIntersection.html)
 */
fun lineCircleIntersection(
    center: Vector,
    startVector: Vector,
    endVector: Vector,
    r: Double
): List<Vector> {
    val start = startVector - center
    val end = endVector - center
    val deltas = end - start
    val dr = deltas.norm
    val D = start cross end
    val discriminant = r * r * dr * dr - D * D

    // discriminant = 0 for 1 intersection, >0 for 2
    val intersections = ArrayList<Vector>()
    val xLeft = D * deltas.y
    val yLeft = -D * deltas.x
    val xRight = stupidSign(deltas.y) * deltas.x * sqrt(discriminant)
    val yRight = deltas.y.absoluteValue * sqrt(discriminant)
    val div = deltas.norm.pow(2)
    if (discriminant == 0.0) {
        intersections.add(Vector(xLeft / div, yLeft / div))
    } else {
        // add 2 points, one with positive right side and one with negative right side
        intersections.add(Vector((xLeft + xRight) / div, (yLeft + yRight) / div))
        intersections.add(Vector((xLeft - xRight) / div, (yLeft - yRight) / div))
    }
    return intersections.map { it + center }
}