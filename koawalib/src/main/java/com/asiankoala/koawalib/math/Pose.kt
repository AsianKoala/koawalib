package com.asiankoala.koawalib.math

import com.acmerobotics.roadrunner.geometry.Pose2d
import kotlin.math.cos
import kotlin.math.sin

class Pose(
    val x: Double = 0.0,
    val y: Double = 0.0,
    val heading: Double = 0.0
) {
    constructor(x: Int, y: Int, h: Int) : this(x.d, y.d, h.d)
    constructor(p: Vector, h: Double) : this(p.x, p.y, h)
    constructor(p: Pose2d) : this(p.x, p.y, p.heading)

    val vec get() = Vector(x, y)

    fun toPose2d(): Pose2d {
        return Pose2d(x, y, heading)
    }

    fun plusWrap(other: Pose) = Pose(x + other.x, y + other.y, (heading + other.heading).angleWrap)

    fun scale(scalar: Double) = Pose(x * scalar, y * scalar, heading * scalar)

    operator fun plus(other: Pose) = Pose(x + other.x, y + other.y, heading + other.heading)

    fun rawString(): String {
        return String.format("%.2f, %.2f, %.2f", x, y, heading)
    }

    override fun toString(): String {
        return String.format("%.2f, %.2f, %.2f", x, y, heading.degrees)
    }
}
