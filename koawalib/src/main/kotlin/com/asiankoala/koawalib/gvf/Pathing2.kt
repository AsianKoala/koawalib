package com.asiankoala.koawalib.gvf

import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.d
import kotlin.math.pow

object Pathing2 {
    data class DifferentiablePoint(
        val x: Double = 0.0,
        val first: Double = 0.0,
        val second: Double = 0.0,
        val third: Double = 0.0
    ) {
        val l = listOf(x, first, second, third)
    }

    fun Pair<DifferentiablePoint, DifferentiablePoint>.vectorize(): List<Vector> {
        return first.l.zip(second.l).map { Vector(it.first, it.second) }
    }

    abstract class DifferentiableFunction {
        abstract operator fun get(t: Double): DifferentiablePoint
    }

    class Quintic(
        start: DifferentiablePoint,
        end: DifferentiablePoint
    ) : DifferentiableFunction() {
        private val coeffs = mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

        override operator fun get(t: Double): DifferentiablePoint {
            return DifferentiablePoint(
                coeffs[0] * t.pow(5) + coeffs[1] * t.pow(4) + coeffs[2] * t.pow(3) +
                        coeffs[3] * t.pow(2) + coeffs[4] * t + coeffs[5],
                5 * coeffs[0] * t.pow(4) + 4 * coeffs[1] * t.pow(3) + 3 * coeffs[2] * t.pow(2) +
                        2 * coeffs[3] * t + coeffs[4],
                20 * coeffs[0] * t.pow(3) + 12 * coeffs[1] * t.pow(2) + 6 * coeffs[2] * t + 2 * coeffs[3]
            )
        }

        init {
            /**
             * https://www.wolframalpha.com/input?i=row+echelon+form+%5B%5B0%2C0%2C0%2C0%2C0%2Cf%2Cu%5D%2C%5B0%2C0%2C0%2C0%2Ce%2C0%2Cv%5D%2C%5B0%2C0%2C0%2C2d%2C0%2C0%2Cw%5D%2C%5Ba%2Cb%2Cc%2Cd%2Ce%2Cf%2Cx%5D%2C%5B5a%2C4b%2C3c%2C2d%2C1e%2C0%2Cy%5D%2C%5B20a%2C12b%2C6c%2C2d%2C0%2C0%2Cz%5D%5D
             *
             * tldr to find coeffs, just shove it into a row echelon form
             * and then just calc it
             */

            // coeffs: a,b,c,d,e,f
            // u, v, w = start diff
            // x, y, z = end diff

            // 2 = 2u/f
            // 2f = 2u
            // f = u
            coeffs[5] = start.x

            // 2 = 2v/e
            // 2e = 2v
            // e = v
            coeffs[4] = start.first

            // 2 = w/d
            // 2d = w
            // d = w/2
            coeffs[3] = start.second / 2.0

            // 2 = -(20u + 12v + 3w - 20x + 8y - z) / c
            // c = -(20u + 12v + 3w - 20x + 8y - z) / 2
            coeffs[2] = -(20 * start.x + 12 * start.first + 3 * start.second
                    - 20 * end.first + 8 * end.second - end.second) / 2.0

            // 2 = (30u + 16v + 3w -30x + 14y - 2z) / b
            // b = (30u + 16v + 3w -30x + 14y - 2z) / 2
            coeffs[1] = (30 * start.x + 16 * start.first + 3 * start.second
                    - 30 * end.x + 14 * end.first - 2 * end.second) / 2.0

            // 2 = -(12u + 6v + w - 12x + 6y -z) / a
            // a = -(12u + 6v + w - 12x + 6y -z) / 2
            coeffs[0] = -(12 * start.x + 6 * start.first + start.second
                    - 12 * end.x + 6 * end.first - end.second) / 2.0
        }
    }

    // used to represent unknown parameter
    abstract class Spline(
        private val x: DifferentiableFunction,
        private val y: DifferentiableFunction
    ) {
        open operator fun get(t: Double): List<Vector> {
            return Pair(x[t], y[t]).vectorize()
        }

        abstract val length: Double
        val end get() = this[length]
        val start get() = this[0.0]
    }

    class PiecewiseLinearFunction(
        private val segments: List<Vector>
    ) : DifferentiableFunction() {
        // represent a line as difference of two vectors
        override fun get(t: Double): DifferentiablePoint {
            var running = 0.0
            for(i in 1 until segments.size) {
                val line = segments[i] - segments[i-1]
                if(line.norm + running > t) {
                    // my point is start + t * line
                    return DifferentiablePoint(segments[i-1] + t * line)
                }
            }
        }

    }

    class FlattenedCurve(
        spline: Spline,
        segmentCount: Int
    ) : Spline {
        private var l = 0.0
        override val length: Double = l // todo: does this even work? xd

        val x: DifferentiableFunction
        val y: DifferentiableFunction

        init {
            val step = spline.length * (1.0 / segmentCount.d)
            val coordinates = mutableListOf(spline.start[0])
            l = 0.0
            for(i in 0..segmentCount) {
                val t = i * step
                val c = spline[t][0]
                val prev = coordinates.last()
                l += prev dist c
                coordinates.add(c)
            }
        }
    }

    /*
    see https://www.youtube.com/watch?v=unWguclP-Ds&list=PLC8FC40C714F5E60F&index=2
    and https://pomax.github.io/bezierinfo/#arclength
    arc length is int 0->1 sqrt(f_x^2 + f_y^2) dt of course
    work in progress kek
     */
    class GuassianQuadrature(
        private val steps: Int,
        private val z: Double,
        private val cTerms: List<Double>,
        private val tTerms: List<Double>
    ) {
        fun indSum(i: Int): Double {

        }

        init {
            require(steps == cTerms.size)
            require(steps == tTerms.size)
        }
    }

    class Curve(
        x: Quintic,
        y: Quintic,
    ) : Spline(x, y) {
        // ok this shouldn't be t but FUCK idk whatever
        override operator fun get(t: Double): List<Vector> {
            TODO()
        }
    }
}