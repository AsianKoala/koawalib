package com.asiankoala.koawalib.math

import kotlin.math.atan2
import kotlin.math.hypot

/**
 * Represents 2D Vectors
 * @property norm norm (magnitude) of the vector
 * @property angle angle vector makes with the x axis
 */
data class Vector(
    val x: Double = 0.0,
    val y: Double = 0.0
) {
    val norm get() = hypot(x, y)
    val angle get() = atan2(y, x)
    val unit get() = this / norm
    val asN get() = NVector(x, y)

    infix fun dot(other: Vector): Double = other.x * this.x + other.y * this.y
    infix fun cross(other: Vector): Double = x * other.y - y * other.x
    infix fun dist(other: Vector): Double = (this - other).norm

    fun rotate(angle: Double) = Vector(
        x * angle.cos - y * angle.sin,
        x * angle.sin + y * angle.cos
    )

    operator fun plus(vector: Vector) = Vector(x + vector.x, y + vector.y)
    operator fun minus(vector: Vector) = Vector(x - vector.x, y - vector.y)
    operator fun times(scalar: Double) = Vector(x * scalar, y * scalar)
    operator fun div(scalar: Double) = Vector(x / scalar, y / scalar)
    operator fun unaryMinus() = this * -1.0

    override fun toString() = String.format("%.2f, %.2f", x, y)

    companion object {
        fun fromPolar(mag: Double, angle: Double) = Vector(mag).rotate(angle)
    }
}
