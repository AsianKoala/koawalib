package com.asiankoala.koawalib.math

import com.asiankoala.koawalib.math.MathUtil.d
import com.asiankoala.koawalib.math.MathUtil.degrees
import com.asiankoala.koawalib.math.MathUtil.wrap

data class Pose(
    val x: Double = 0.0,
    val y: Double = 0.0,
    val heading: Double = 0.0
) {
    constructor(x: Int, y: Int, h: Int) : this(x.d, y.d, h.d)
    constructor(p: Point, h: Double) : this(p.x, p.y, h)

    val point = Point(x, y)

    operator fun plus(other: Pose) = Pose(x + other.x, y + other.y, (heading + other.heading).wrap)
    operator fun minus(other: Pose) = this + -other
    operator fun unaryMinus() = Pose(-x, -y, -heading)

    val degString get() = "Pose(x=$x, y=$y, heading=${heading.degrees}"
}
