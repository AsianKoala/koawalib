package com.asiankoala.koawalib.math

/**
 * Represents robot's position and heading
 * @param[x] x position
 * @param[y] y position
 * @param[heading] heading, in radians
 */
data class Pose(
    val x: Double = 0.0,
    val y: Double = 0.0,
    val heading: Double = 0.0
) {
    /**
     * Secondary constructor that takes a vector and heading
     * @param[v] vector
     * @param[h] heading
     */
    constructor(v: Vector, h: Double) : this(v.x, v.y, h)

    /**
     * This pose's position vector
     */
    val vec get() = Vector(x, y)

    /**
     * String of x, y, and heading in degrees
     */
    override fun toString(): String {
        return String.format("%.2f, %.2f, %.2f°", x, y, heading.degrees)
    }
}
