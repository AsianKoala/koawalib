package com.asiankoala.koawalib.math

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Represents 2D Vectors
 * @param[x] x value
 * @param[y] y value
 */
data class Vector(
    val x: Double = 0.0,
    val y: Double = 0.0
) {
    /**
     * Magnitude of this vector
     */
    val norm get() = hypot(x, y)

    /**
     * Angle the vector makes with the x axis
     */
    val angle get() = atan2(y, x)

    /**
     * Unit vector
     */
    val unit get() = this / norm

    infix fun dot(other: Vector): Double = other.x * this.x + other.y * this.y
    infix fun cross(other: Vector): Double = x * other.y - y * other.x
    infix fun dist(other: Vector): Double = (this - other).norm

    fun rotate(angle: Double) = Vector(
        x * cos(angle) - y * sin(angle),
        x * sin(angle) + y * cos(angle)
    )

    operator fun plus(vector: Vector) = Vector(x + vector.x, y + vector.y)
    operator fun minus(vector: Vector) = Vector(x - vector.x, y - vector.y)
    operator fun times(scalar: Double) = Vector(x * scalar, y * scalar)
    operator fun div(scalar: Double) = Vector(x / scalar, y / scalar)
    operator fun unaryMinus() = this * -1.0

    override fun toString() = String.format("%.2f, %.2f", x, y)

    companion object {
        /**
         * Create a vector from polar coordinates
         * @param[mag] Magnitude of the vector
         * @param[angle] Angle of the vector
         */
        fun fromPolar(mag: Double, angle: Double) = Vector(mag).rotate(angle)
    }
}
