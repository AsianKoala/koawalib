package com.asiankoala.koawalib.math

import com.acmerobotics.roadrunner.geometry.Vector2d
import kotlin.math.*

/**
 * Represents 2D Vectors
 * @property norm norm (magnitude) of the vector
 * @property angle angle vector makes with the x axis
 */
data class Vector(
    val x: Double = 0.0,
    val y: Double = 0.0
) {
    constructor(x: Int, y: Int) : this(x.d, y.d)
    constructor(v: Vector) : this(v.x, v.y)

    val norm get() = hypot(x, y)
    val angle get() = atan2(y, x)

    infix fun dot(other: Vector): Double {
        return other.x * this.x + other.y * this.y
    }

    infix fun cross(other: Vector): Double {
        return x * other.y - y * other.x
    }

    fun rotate(angle: Double) = Vector(
        x * angle.cos - y * angle.sin,
        x * angle.sin + y * angle.cos
    )

    fun dist(other: Vector): Double {
        return sqrt(this dot other)
    }

    fun vec(): Vector2d {
        return Vector2d(x, y)
    }

    operator fun plus(vector: Vector) = Vector(x + vector.x, y + vector.y)
    operator fun minus(vector: Vector) = Vector(x - vector.x, y - vector.y)
    operator fun times(scalar: Double) = Vector(x * scalar, y * scalar)
    operator fun div(scalar: Double) = Vector(x / scalar, y / scalar)
    operator fun unaryMinus() = this * -1.0

    override fun toString() = String.format("%.2f, %.2f", x, y)
}
