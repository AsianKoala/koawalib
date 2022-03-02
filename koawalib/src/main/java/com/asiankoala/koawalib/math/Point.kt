package com.asiankoala.koawalib.math

import com.asiankoala.koawalib.math.MathUtil.cos
import com.asiankoala.koawalib.math.MathUtil.d
import com.asiankoala.koawalib.math.MathUtil.sin
import kotlin.math.atan2
import kotlin.math.hypot

data class Point(
    val x: Double = 0.0,
    val y: Double = 0.0
) {
    constructor(x: Int, y: Int) : this(x.d, y.d)
    val hypot = hypot(x, y)
    val atan2 = atan2(y, x)

    operator fun plus(point: Point) = Point(x + point.x, y + point.y)
    operator fun minus(point: Point) = Point(x - point.x, y - point.y)
    operator fun times(scalar: Double) = Point(x * scalar, y * scalar)
    operator fun div(scalar: Double) = Point(x / scalar, y / scalar)
    operator fun unaryMinus() = this * -1.0

    fun distance(point: Point) = (this - point).hypot

    fun rotate(angle: Double) = Point(
        x * angle.cos - y * angle.sin,
        x * angle.sin + y * angle.cos
    )

    override fun toString() = String.format("%.2f, %.2f", x, y)
}
