package com.asiankoala.koawalib.math

import com.acmerobotics.roadrunner.geometry.Pose2d
import kotlin.math.cos
import kotlin.math.sin

class Pose(
    x: Double = 0.0,
    y: Double = 0.0,
    val heading: Double = 0.0
) : Vector(x, y) {
    constructor(x: Int, y: Int, h: Int) : this(x.d, y.d, h.d)
    constructor(p: Vector, h: Double) : this(p.x, p.y, h)
    constructor(p: Pose2d) : this(p.x, p.y, p.heading)

    val vec get() = Vector(x, y)

    fun toPose2d(): Pose2d {
        return Pose2d(x, y, heading)
    }

    fun plusWrap(other: Pose) = Pose(x + other.x, y + other.y, (heading + other.heading).angleWrap)

    fun directionVector(): Vector {
        return Vector(cos(heading), sin(heading)).normalized()
    }

    fun translate(r: Vector): Vector {
        val dx = r.x - x
        val dy = r.y - y
        val c = cos(heading)
        val s = sin(heading)
        return Vector(
            dx * c + dy * s,
            dx * -s + dy * c
        )
    }

    fun translate(p: Pose): Pose {
        val vec = translate(p as Vector)
        return Pose(vec.x, vec.y, p.heading - heading)
    }

    fun invTranslate(r: Vector): Vector {
        val c = cos(heading)
        val s = sin(heading)
        val dxp = r.x * c - r.y * s
        val dyp = r.x * s + r.y * c
        return Vector(x + dxp, y + dyp)
    }

    operator fun plus(other: Pose) = Pose(x + other.x, y + other.y, heading + other.heading)

    fun rawString(): String {
        return String.format("%.2f, %.2f, %.2f", x, y, heading)
    }

    override fun toString(): String {
        return String.format("%.2f, %.2f, %.2f", x, y, heading.degrees)
    }
}
