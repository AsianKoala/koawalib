package com.asiankoala.koawalib.math

/**
 * Represents robot's position and heading
 * @param x x position
 * @param y y position
 * @param heading heading, in radians
 * @property vec vector made from [x] and [y]
 */
data class Pose(
    val x: Double = 0.0,
    val y: Double = 0.0,
    val heading: Double = 0.0
) {
    constructor(p: Vector, h: Double) : this(p.x, p.y, h)

    val vec get() = Vector(x, y)

    /**
     * Add two poses together, while wrapping the heading to [-pi, pi]
     */
    fun plusWrap(other: Pose) = Pose(x + other.x, y + other.y, (heading + other.heading).angleWrap)

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
