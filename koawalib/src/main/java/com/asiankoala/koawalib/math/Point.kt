package com.asiankoala.koawalib.math

import org.apache.commons.math3.linear.*
import kotlin.math.*

open class Point(
    val x: Double = 0.0,
    val y: Double = 0.0
) {
    companion object {
        fun fromApacheVec(v: RealVector): Point {
            return Point(v.getEntry(0), v.getEntry(1))
        }

        fun fromAngle(angle: Double): Point {
            return Point(cos(angle), sin(angle))
        }

        fun arePointsCollinear(r1: Point, r2: Point, r3: Point): Boolean {
            val denomMat = Array2DRowRealMatrix(
                arrayOf(
                    doubleArrayOf(r1.x, r1.y, 1.0),
                    doubleArrayOf(r2.x, r2.y, 1.0),
                    doubleArrayOf(r3.x, r3.y, 1.0)
                )
            )
            return LUDecomposition(denomMat).determinant == 0.0
        }
    }

    constructor(x: Int, y: Int) : this(x.d, y.d)

    val hypot get() = hypot(x, y)
    val atan2 get() = atan2(y, x)

    fun dot(other: Point): Double {
        return other.x * this.x + other.y * this.y
    }

    fun norm(): Double {
        return this.sqNorm().pow(0.5)
    }

    fun sqNorm(): Double {
        return x.pow(2) + y.pow(2)
    }

    fun normalized(): Point {
        val len = this.norm()
        return Point(x / len, y / len)
    }

    fun getRightNormal(): Point {
        return Point(y, -x)
    }
    fun getLeftNormal(): Point {
        return Point(-y, x)
    }

    fun toApacheVec(): RealVector {
        return ArrayRealVector(doubleArrayOf(this.x, this.y))
    }

    fun outerProduct(): RealMatrix {
        return Array2DRowRealMatrix(
            arrayOf(
                doubleArrayOf(x * x, x * y),
                doubleArrayOf(x * y, y * y)
            )
        )
    }

    fun zProd(other: Point): Double {
        return this.x * other.y - this.y * other.x
    }

    fun scalarMul(d: Double): Point {
        return Point(x * d, y * d)
    }

    fun scalarDiv(d: Double): Point {
        return scalarDiv(1 / d)
    }

    fun distance(point: Point) = (this - point).hypot

    fun rotate(angle: Double) = Point(
        x * angle.cos - y * angle.sin,
        x * angle.sin + y * angle.cos
    )

    fun sqDist(other: Point): Double {
        return (other.x - x).pow(2) + (other.y - y).pow(2)
    }
    fun dist(other: Point): Double {
        return sqDist(other).pow(0.5)
    }

    fun neg(): Point {
        return this.scalarMul(-1.0)
    }

    fun divide(other: Point): Double {
        return norm() / other.norm()
    }

    operator fun plus(point: Point) = Point(x + point.x, y + point.y)
    operator fun minus(point: Point) = Point(x - point.x, y - point.y)
    operator fun unaryMinus() = scalarMul(-1.0)

    override fun toString() = String.format("%.2f, %.2f", x, y)

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Point

        if (x != other.x) return false
        if (y != other.y) return false
        if (hypot != other.hypot) return false
        if (atan2 != other.atan2) return false

        return true
    }
}
