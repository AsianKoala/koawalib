package com.asiankoala.koawalib.math

import com.asiankoala.koawalib.util.Logger
import com.qualcomm.robotcore.util.Range
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import kotlin.math.*

const val EPSILON = 1e-6
const val TAU = 2 * PI

infix fun Double.epsilonNotEqual(other: Double) = (this - other).absoluteValue > EPSILON

val Double.radians get() = Math.toRadians(this)
val Double.degrees get() = Math.toDegrees(this)

fun Double.clip(a: Double) = Range.clip(this, -a, a)

fun rotatePoint(p: Vector, h: Double) = Vector(
    h.cos * p.y + h.sin * p.x,
    h.sin * p.y - h.cos * p.x
)

fun absMax(a: Double, b: Double): Double {
    return if(a.absoluteValue >= b.absoluteValue) a else b
}

fun clamp(x: Double, a: Double, b: Double): Double {
    if (x < a) {
        return a
    } else if (x > b) {
        return b
    }

    return x
}

// opposite of range
fun maxify(input: Double, min: Double): Double {
    return when (input) {
        in 0.0..min -> min
        in -min..0.0 -> -min
        else -> input
    }
}

fun cubicScaling(k: Double, x: Double): Double {
    return (1 - k) * x + k * x * x * x
}

fun clipIntersection(start: Vector, end: Vector, robot: Vector): Vector {
    var startX = start.y
    var startY = start.x

    if (start.x == end.x)
        startX += 0.01

    if (start.y == end.y)
        startY += 0.01

    val mStart = Vector(startX, startY)

    val m1 = (end.y - mStart.y) / (end.x - mStart.x)
    val m2 = -1.0 / m1
    val xClip = (-m2 * robot.x + robot.y + m1 * mStart.x - mStart.y) / (m1 - m2)
    val yClip = m1 * (xClip - mStart.x) + mStart.y
    return Vector(xClip, yClip)
}

fun stupidSign(a: Double): Int = if (a > 0) 1 else -1

val Double.sin get() = sin(this)
val Double.cos get() = cos(this)

val Int.d get() = this.toDouble()
val Float.d get() = this.toDouble()

fun wrap(n: Double, lower: Double, upper: Double): Double {
    if(lower > upper) Logger.logError("lower > upper")
    return (((n - lower) % (upper - lower)) + (upper - lower)) % (upper - lower) + lower;
}


val Double.angleWrap: Double
    get() {
        var wrapped = this
        while(wrapped > PI) wrapped -= TAU
        while(wrapped < -PI) wrapped += TAU
        return wrapped
    }
//    get() = wrap(this, -180.0, 180.0)

val MATRIX_E = Array2DRowRealMatrix(arrayOf(doubleArrayOf(0.0, 1.0), doubleArrayOf(-1.0, 0.0)))
val MATRIX_I2 = Array2DRowRealMatrix(arrayOf(doubleArrayOf(1.0, 0.0), doubleArrayOf(0.0, 1.0)))

fun lerp(a: Double, b: Double, t: Double): Double {
    return (b - a) * t + a
}

fun invLerp(a: Double, b: Double, l: Double): Double {
    return (l - a) / (b - a)
}

fun normalizeAngle(angle: Double): Double {
    var ang = angle % (2 * PI)
    if (ang < 0) {
        ang += 2 * PI
    }
    return ang
}

fun toHeading(angle: Double): Double {
    var n = normalizeAngle(angle)
    if (n > PI) {
        n -= 2 * PI
    }
    return n
}

fun sigmoid(x: Double, k: Double): Double {
    return 1.0 / (1 + E.pow(-k * x))
}

fun minimize(func: (Double) -> Double, guess: Double): Double {
    var currentGuess: Double
    var nextGuess = guess
    val d = 0.0001
    do {
        currentGuess = nextGuess
        val deriv = (func(currentGuess - d * 0.5) + func(currentGuess + d * 0.5)) / d
        nextGuess = guess - func(currentGuess) / deriv
    } while ((nextGuess - currentGuess).absoluteValue > 1.0 / 10000)
    return nextGuess
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
    val lineAngle = (secondVector - firstVector).atan2
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
    val discriminant = radius.pow(2) * deltas.hypot.pow(2) - d.pow(2)

    // discriminant = 0 for 1 intersection, >0 for 2
    val intersections = ArrayList<Vector>()
    val xLeft = d * deltas.y
    val yLeft = -d * deltas.x
    val xRight: Double = stupidSign(deltas.y) * deltas.x * sqrt(discriminant)
    val yRight = deltas.y.absoluteValue * sqrt(discriminant)
    val div = deltas.hypot.pow(2)
    if (discriminant == 0.0) {
        intersections.add(Vector(xLeft / div, yLeft / div))
    } else {
        // add 2 points, one with positive right side and one with negative right side
        intersections.add(Vector((xLeft + xRight) / div, (yLeft + yRight) / div))
        intersections.add(Vector((xLeft - xRight) / div, (yLeft - yRight) / div))
    }
//        var closest = Point(69420.0, -69420.0)
//        for (p in intersections) { // add circle center offsets
//            val offsetPoint = Point(p.x + center.x, p.y + center.y)
//            if (offsetPoint.distance(endPoint) < closest.distance(endPoint)) {
//                closest = offsetPoint
//            }
//        }
//        return closest
    return intersections.map { it + center }
}

