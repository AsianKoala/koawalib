package com.asiankoala.koawalib.math

import com.acmerobotics.roadrunner.geometry.Pose2d

/**
 * Represents robot's position and heading
 * @param x x position
 * @param y y position
 * @param heading heading, in radians
 * @property vec vector made from [x] and [y]
 */
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

    /**
     * Add two poses together, while wrapping the heading to [-pi, pi]
     */
    fun plusWrap(other: Pose) = Pose(x + other.x, y + other.y, (heading + other.heading).angleWrap)

    /**
     * Add two poses together, without wrapping
     */
    operator fun plus(other: Pose) = Pose(x + other.x, y + other.y, heading + other.heading)

    /**
     * Multiply pose by a scalar
     */
    operator fun times(scalar: Double) = Pose(x * scalar, y * scalar, heading * scalar)

    /**
     * String of x, y, and heading data
     */
    fun rawString(): String {
        return String.format("%.2f, %.2f, %.2f", x, y, heading)
    }

    /**
     * String of x, y, and heading in degrees
     */
    override fun toString(): String {
        return String.format("%.2f, %.2f, %.2f°", x, y, heading.degrees)
    }
}
