package com.asiankoala.koawalib.math

import com.qualcomm.robotcore.util.Range
import kotlin.math.*

object MathUtil {
    const val EPSILON = 1e-6
    const val TAU = 2 * PI

    infix fun Double.epsilonNotEqual(other: Double) = (this - other).absoluteValue > EPSILON

    val Double.radians get() = Math.toRadians(this)
    val Double.degrees get() = Math.toDegrees(this)

    fun Double.clip(a: Double) = Range.clip(this, -a, a)

    fun rotatePoint(p: Point, h: Double) = Point(
        h.cos * p.y + h.sin * p.x,
        h.sin * p.y - h.cos * p.x
    )

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

    fun clipIntersection(start: Point, end: Point, robot: Point): Point {
        var startX = start.y
        var startY = start.x

        if (start.x == end.x)
            startX += 0.01

        if (start.y == end.y)
            startY += 0.01

        val mStart = Point(startX, startY)

        val m1 = (end.y - mStart.y) / (end.x - mStart.x)
        val m2 = -1.0 / m1
        val xClip = (-m2 * robot.x + robot.y + m1 * mStart.x - mStart.y) / (m1 - m2)
        val yClip = m1 * (xClip - mStart.x) + mStart.y
        return Point(xClip, yClip)
    }

    fun extendLine(firstPoint: Point, secondPoint: Point, distance: Double): Point {
        val lineAngle = (secondPoint - firstPoint).atan2
        val length = secondPoint.distance(firstPoint)
        val extendedLineLength = length + distance

        val extendedX = lineAngle.cos * extendedLineLength + firstPoint.x
        val extendedY = lineAngle.sin * extendedLineLength + firstPoint.y
        return Point(extendedX, extendedY)
    }

    /**
     * @param center          center point of circle
     * @param startPoint start point of the line segment
     * @param endPoint   end point of the line segment
     * @param radius     radius of the circle
     * @return intersection point closest to endPoint
     * @see [https://mathworld.wolfram.com/Circle-LineIntersection.html](https://mathworld.wolfram.com/Circle-LineIntersection.html)
     */
    fun circleLineIntersection(
        center: Point,
        startPoint: Point,
        endPoint: Point,
        radius: Double
    ): Point {
        val start = startPoint - center
        val end = endPoint - center
        val deltas = end - start
        val d = start.x * end.y - end.x * start.y
        val discriminant = radius.pow(2) * deltas.hypot.pow(2) - d.pow(2)

        // discriminant = 0 for 1 intersection, >0 for 2
        val intersections = ArrayList<Point>()
        val xLeft = d * deltas.y
        val yLeft = -d * deltas.x
        val xRight: Double = stupidSign(deltas.y) * deltas.x * sqrt(discriminant)
        val yRight = deltas.y.absoluteValue * sqrt(discriminant)
        val div = deltas.hypot.pow(2)
        if (discriminant == 0.0) {
            intersections.add(Point(xLeft / div, yLeft / div))
        } else {
            // add 2 points, one with positive right side and one with negative right side
            intersections.add(Point((xLeft + xRight) / div, (yLeft + yRight) / div))
            intersections.add(Point((xLeft - xRight) / div, (yLeft - yRight) / div))
        }
        var closest = Point(69420.0, -69420.0)
        for (p in intersections) { // add circle center offsets
            val offsetPoint = Point(p.x + center.x, p.y + center.y)
            if (offsetPoint.distance(endPoint) < closest.distance(endPoint)) {
                closest = offsetPoint
            }
        }
        return closest
    }

    fun stupidSign(a: Double): Int = if (a > 0) 1 else -1

    val Double.sin get() = sin(this)
    val Double.cos get() = cos(this)

    val Int.d get() = this.toDouble()
    val Float.d get() = this.toDouble()

    val Double.wrap: Double
        get() {
            var angle = this
            while (angle > PI) angle -= 2 * PI
            while (angle < -PI) angle += 2 * PI
            return angle
        }
}
