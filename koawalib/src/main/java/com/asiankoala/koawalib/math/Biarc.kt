package com.asiankoala.koawalib.math

import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.LUDecomposition
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin

class Biarc {
    abstract class BiarcPart {
        abstract fun r(t: Double): Point
        abstract fun length(): Double
        abstract fun project(p: Point): Double
        abstract fun tangentVec(t: Double): Point
        abstract fun curvature(toWrappedSpace: Double): Double
    }

    open class LineSegment(val begin: Point, val end: Point) : BiarcPart() {
        override fun curvature(toWrappedSpace: Double): Double {
            return 0.0
        }

        private val line = end.minus(begin)
        override fun r(t: Double): Point {
            return begin + line.scalarMul(t)
        }

        override fun project(p: Point): Double {
            val t = (begin + p).dot(line)
            if (t in 0.0..1.0) {
                return t
            }
            throw IllegalArgumentException("Point outside projection domain")
        }

        override fun length(): Double {
            return line.norm()
        }

        override fun tangentVec(t: Double): Point {
            return line.normalized()
        }
    }

    open class ArcSegment(
        val center: Point,
        val radius: Double,
        val startAngle: Double,
        val endAngle: Double
    ) : BiarcPart() {
        override fun curvature(t: Double): Double {
            return 1.0 / radius
        }

        companion object {
            fun fromThreePoints(ptBegin: Point, ptMid: Point, ptEnd: Point): ArcSegment {
                val hNum = Array2DRowRealMatrix(
                    arrayOf(
                        doubleArrayOf(ptBegin.sqNorm(), ptBegin.y, 1.0),
                        doubleArrayOf(ptMid.sqNorm(), ptMid.y, 1.0),
                        doubleArrayOf(ptEnd.sqNorm(), ptEnd.y, 1.0)
                    )
                )
                val denomMat = Array2DRowRealMatrix(
                    arrayOf(
                        doubleArrayOf(ptBegin.x, ptBegin.y, 1.0),
                        doubleArrayOf(ptMid.x, ptMid.y, 1.0),
                        doubleArrayOf(ptEnd.x, ptEnd.y, 1.0)
                    )
                )
                val denom = (2 * LUDecomposition(denomMat).determinant)
                val h = LUDecomposition(hNum).determinant / denom

                val kNum = Array2DRowRealMatrix(
                    arrayOf(
                        doubleArrayOf(ptBegin.x, ptBegin.sqNorm(), 1.0),
                        doubleArrayOf(ptMid.x, ptMid.sqNorm(), 1.0),
                        doubleArrayOf(ptEnd.x, ptEnd.sqNorm(), 1.0)
                    )
                )
                val k = LUDecomposition(kNum).determinant / denom

                val center_ = Point(h, k)
                val radius_ = center_.dist(ptBegin)
                val beginAngle_ = (ptBegin - center_).atan2
                val endAngle_ = (ptEnd - center_).atan2
                return ArcSegment(center_, radius_, beginAngle_, endAngle_)
            }
        }

        fun angleFromT(t: Double): Double {
            return lerp(startAngle, endAngle, t)
        }

        override fun r(t: Double): Point {
            val ang = angleFromT(t)
            return center + Point(cos(ang), sin(ang)).scalarMul(radius)
        }

        override fun length(): Double {
            return (endAngle - startAngle).absoluteValue * radius
        }

        override fun project(p: Point): Double {
            val angle = (center - p).atan2
            val oppAngle = angle + PI
            val negAngle = angle - PI
            return when {
                angle in this -> invertAngle(angle)
                oppAngle in this -> invertAngle(oppAngle)
                negAngle in this -> invertAngle(negAngle)
                else -> throw IllegalArgumentException("Point outside projection domain")
            }
        }

        fun invertAngle(angle: Double): Double {
            return invLerp(startAngle, endAngle, angle)
        }

        override fun toString(): String {
            return "ArcSegment($center, $radius, $startAngle, $endAngle)"
        }

        override fun tangentVec(t: Double): Point {
            val ang = angleFromT(t)
            val dir = if (startAngle < endAngle) 1.0 else -1.0
            return Point(-sin(ang), cos(ang)).scalarMul(dir)
        }

        operator fun contains(angle: Double): Boolean {
            return invertAngle(angle) in 0.0..1.0
        }
    }

    class AugmentedArc(
        center: Point,
        radius: Double,
        startAngle: Double,
        endAngle: Double,
        val kBegin: Double,
        val kEnd: Double
    ) : ArcSegment(center, radius, startAngle, endAngle) {
        companion object {
            fun fromThreePoints(
                ptBegin: Point,
                ptMid: Point,
                ptEnd: Point,
                kBegin: Double,
                kEnd: Double
            ): AugmentedArc {
                val hNum = Array2DRowRealMatrix(
                    arrayOf(
                        doubleArrayOf(ptBegin.sqNorm(), ptBegin.y, 1.0),
                        doubleArrayOf(ptMid.sqNorm(), ptMid.y, 1.0),
                        doubleArrayOf(ptEnd.sqNorm(), ptEnd.y, 1.0)
                    )
                )
                val denomMat = Array2DRowRealMatrix(
                    arrayOf(
                        doubleArrayOf(ptBegin.x, ptBegin.y, 1.0),
                        doubleArrayOf(ptMid.x, ptMid.y, 1.0),
                        doubleArrayOf(ptEnd.x, ptEnd.y, 1.0)
                    )
                )
                val denom = (2 * LUDecomposition(denomMat).determinant)
                val h = LUDecomposition(hNum).determinant / denom

                val kNum = Array2DRowRealMatrix(
                    arrayOf(
                        doubleArrayOf(ptBegin.x, ptBegin.sqNorm(), 1.0),
                        doubleArrayOf(ptMid.x, ptMid.sqNorm(), 1.0),
                        doubleArrayOf(ptEnd.x, ptEnd.sqNorm(), 1.0)
                    )
                )
                val k = LUDecomposition(kNum).determinant / denom

                val center_ = Point(h, k)
                val radius_ = center_.dist(ptBegin)
                val beginAngle_ = normalizeAngle((ptBegin - center_).atan2)
                val endAngle_ = normalizeAngle((ptEnd - center_).atan2)
                return AugmentedArc(center_, radius_, beginAngle_, endAngle_, kBegin, kEnd)
            }
        }

        override fun curvature(t: Double): Double {
            return lerp(kBegin, kEnd, t)
        }
    }

    class AugmentedLine(
        val ptBegin: Point,
        val ptEnd: Point,
        val kBegin: Double,
        val kEnd: Double
    ) :
        LineSegment(ptBegin, ptEnd) {

        override fun curvature(t: Double): Double {
            return lerp(kBegin, kEnd, t)
        }
    }

    class BiarcPartWrapper(val wrapped: BiarcPart, var begin: Double, var end: Double) {
        fun length(): Double {
            return wrapped.length()
        }

        private fun toWrappedSpace(t: Double): Double {
            return invLerp(begin, end, t)
        }

        private fun fromWrappedSpace(t: Double): Double {
            return lerp(begin, end, t)
        }

        fun project(p: Point): Double {
            return fromWrappedSpace(wrapped.project(p))
        }

        fun r(t: Double): Point {
            return wrapped.r(toWrappedSpace(t))
        }

        fun tangentVec(t: Double): Point {
            return wrapped.tangentVec(toWrappedSpace(t))
        }

        operator fun contains(t: Double): Boolean {
            return t in begin..end
        }

        fun curvature(t: Double): Double {
            return wrapped.curvature(toWrappedSpace(t))
        }
    }
}
