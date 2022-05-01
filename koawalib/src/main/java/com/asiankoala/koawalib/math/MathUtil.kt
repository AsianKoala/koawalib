package com.asiankoala.koawalib.math

import com.asiankoala.koawalib.logger.Logger
import kotlin.math.*

const val EPSILON = 1e-6
const val TAU = 2 * PI
const val VOLTAGE_CONSTANT = 12.0

infix fun Double.epsilonNotEqual(other: Double) = (this - other).absoluteValue > EPSILON

val Double.radians get() = Math.toRadians(this)
val Double.degrees get() = Math.toDegrees(this)

fun absMax(a: Double, b: Double): Double {
    return if (a.absoluteValue >= b.absoluteValue) a else b
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
    if (value.sign < 0.0) Logger.logError("value $value must be positive")
}

fun cubicScaling(k: Double, x: Double): Double {
    return (1 - k) * x + k * x * x * x
}

fun inputModulus(input: Double, minimumInput: Double, maximumInput: Double): Double {
    var input = input
    val modulus = maximumInput - minimumInput

    // Wrap input if it's above the maximum input
    val numMax = ((input - minimumInput) / modulus).toInt()
    input -= numMax * modulus

    // Wrap input if it's below the minimum input
    val numMin = ((input - maximumInput) / modulus).toInt()
    input -= numMin * modulus
    return input
}

fun stupidSign(a: Double): Int = if (a > 0) 1 else -1

val Double.sin get() = sin(this)
val Double.cos get() = cos(this)

val Int.d get() = this.toDouble()
val Float.d get() = this.toDouble()

val Double.angleWrap: Double
    get() {
        var wrapped = this
        while (wrapped > PI) wrapped -= TAU
        while (wrapped < -PI) wrapped += TAU
        return wrapped
    }

fun clipToLine(start: Vector, end: Vector, robot: Vector): Vector {
    var startX = start.x
    var startY = start.y

    if (start.x == end.x)
        startX += 0.001

    if (start.y == end.y)
        startY += 0.001

    val mStart = Vector(startX, startY)

    val m1 = (end.y - mStart.y) / (end.x - mStart.x)
    val m2 = -1.0 / m1
    val xClip = (-m2 * robot.x + robot.y + m1 * mStart.x - mStart.y) / (m1 - m2)
    val yClip = m1 * (xClip - mStart.x) + mStart.y
    return Vector(xClip, yClip)
}

fun extendLine(firstVector: Vector, secondVector: Vector, distance: Double): Vector {
    val lineAngle = (secondVector - firstVector).angle
    val length = secondVector.dist(firstVector)
    val extendedLineLength = length + distance

    val extendedX = lineAngle.cos * extendedLineLength + firstVector.x
    val extendedY = lineAngle.sin * extendedLineLength + firstVector.y
    return Vector(extendedX, extendedY)
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
    radius: Double
): List<Vector> {
    val start = startVector - center
    val end = endVector - center
    val deltas = end - start
    val d = start.x * end.y - end.x * start.y
    val discriminant = radius.pow(2) * deltas.norm.pow(2) - d.pow(2)

    // discriminant = 0 for 1 intersection, >0 for 2
    val intersections = ArrayList<Vector>()
    val xLeft = d * deltas.y
    val yLeft = -d * deltas.x
    val xRight: Double = stupidSign(deltas.y) * deltas.x * sqrt(discriminant)
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
