package com.asiankoala.koawalib.math

import kotlin.math.*

const val EPSILON = 1e-6
const val TAU = 2 * PI

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

fun inputModulus(input: Double, minimumInput: Double, maximumInput: Double): Double {
    var inp = input
    val modulus = maximumInput - minimumInput

    val numMax = ((inp - minimumInput) / modulus).toInt()
    inp -= numMax * modulus

    val numMin = ((inp - maximumInput) / modulus).toInt()
    inp -= numMin * modulus
    return inp
}

fun nonzeroSign(a: Double): Int = if (a > 0) 1 else -1

val Double.sin get() = sin(this)
val Double.cos get() = cos(this)

val Int.d get() = this.toDouble()
val Float.d get() = this.toDouble()
val Long.d get() = this.toDouble()

val Double.angleWrap: Double
    get() {
        var wrapped = this % TAU
        wrapped = (wrapped + TAU) % TAU
        return wrapped
    }

val Double.wrap2PI: Double
    get() {
        var wrapped = this.angleWrap
        return if (wrapped > 0.0) wrapped else wrapped + PI
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
    val div = deltas.norm.pow(2)
    val left = Vector(D * deltas.y, -D * deltas.x) / div
    val right = Vector(
        nonzeroSign(deltas.y) * deltas.x * sqrt(discriminant),
        deltas.y.absoluteValue * sqrt(discriminant)
    ) / div
    if (discriminant == 0.0) {
        intersections.add(left)
    } else {
        intersections.add(left + right)
        intersections.add(left - right)
    }
    return intersections.map { it + center }
}
