package com.asiankoala.koawalib.math

import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.util.epsilonEquals
import kotlin.math.*

open class Vector(
    val x: Double = 0.0,
    val y: Double = 0.0
) {
    constructor(x: Int, y: Int) : this(x.d, y.d)
    constructor(v: Vector) : this(v.x, v.y)

    val hypot get() = hypot(x, y)
    val atan2 get() = atan2(y, x)

    infix fun dot(other: Vector): Double {
        return other.x * this.x + other.y * this.y
    }

    infix fun cross(other: Vector): Double {
        return x * other.y - y * other.x
    }

    fun norm(): Double {
        return (x.pow(2) + y.pow(2)).pow(0.5);

    }

    fun normalized(): Vector {
        val len = this.norm()
        return Vector(x / len, y / len)
    }

    fun scale(d: Double): Vector {
        return Vector(x * d, y * d)
    }

    fun scalarDiv(d: Double): Vector {
        return scalarDiv(1 / d)
    }

    fun rotate(angle: Double) = Vector(
        x * angle.cos - y * angle.sin,
        x * angle.sin + y * angle.cos
    )

    fun dist(other: Vector): Double {
        return ((other.x - x).pow(2) + (other.y - y).pow(2)).pow(0.5)
    }

    fun vec(): Vector2d {
        return Vector2d(x, y)
    }

    fun trueNormal(): Vector {
        return if(norm() > 1.0) {
            normalized()
        } else {
            this
        }
    }

    operator fun plus(vector: Vector) = Vector(x + vector.x, y + vector.y)
    operator fun minus(vector: Vector) = Vector(x - vector.x, y - vector.y)
    operator fun unaryMinus() = scale(-1.0)

    override fun toString() = String.format("%.2f, %.2f", x, y)

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vector

        return x epsilonEquals other.x && y epsilonEquals other.y
    }
}
