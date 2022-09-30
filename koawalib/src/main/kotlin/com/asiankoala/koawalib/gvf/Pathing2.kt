package com.asiankoala.koawalib.gvf

import com.asiankoala.koawalib.math.Vector
import kotlin.math.pow

object Pathing2 {
    data class DifferentiablePoint(
        val zero: Double = 0.0,
        val first: Double = 0.0,
        val second: Double = 0.0,
        val third: Double = 0.0
    )

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
             * tldr to find coeffs, just shove it into a row echelon form
             * and then just calc it
             */

            // coeffs: a,b,c,d,e,f
            // u, v, w = start diff
            // x, y, z = end diff

            // 2 = 2u/f
            // 2f = 2u
            // f = u
            coeffs[5] = start.zero

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
            coeffs[2] = -(20 * start.zero + 12 * start.first + 3 * start.second
                    - 20 * end.first + 8 * end.second - end.second) / 2.0

            // 2 = (30u + 16v + 3w -30x + 14y - 2z) / b
            // b = (30u + 16v + 3w -30x + 14y - 2z) / 2
            coeffs[1] = (30 * start.zero + 16 * start.first + 3 * start.second
                    - 30 * end.zero + 14 * end.first - 2 * end.second) / 2.0

            // 2 = -(12u + 6v + w - 12x + 6y -z) / a
            // a = -(12u + 6v + w - 12x + 6y -z) / 2
            coeffs[0] = -(12 * start.zero + 6 * start.first + start.second
                    - 12 * end.zero + 6 * end.first - end.second) / 2.0
        }
    }

    abstract class Parametric(
        private val x: DifferentiableFunction,
        private val y: DifferentiableFunction
    ) {
        open operator fun get(t: Double, n: Int = 0): Vector {
            val xt = x[t]
            val yt = y[t]
            return when(n) {
                1 -> Vector(xt.first, yt.first)
                2 -> Vector(xt.second, yt.second)
                3 -> Vector(xt.third, yt.third)
                else -> Vector(xt.zero, yt.zero)
            }
        }

        abstract val length: Double
        val end get() = this[length]
        val start get() = this[0.0]
    }

    /*
    see https://www.youtube.com/watch?v=unWguclP-Ds&list=PLC8FC40C714F5E60F&index=2
    and https://pomax.github.io/bezierinfo/#arclength
    arc length is int 0->1 sqrt(f_x(t)^2 + f_y(t)^2) dt of course
    Gaussian quadrature is derived on the interval [-1,1]
    but changing it to [0,1] is arbitrarily easy
     */
    data class GaussianLegendreCoefficients(
        val weight: Double,
        val abscissa: Double
    )

    abstract class GaussianQuadratureTable {
        abstract val list: List<GaussianLegendreCoefficients>
    }

    class FivePointGaussianLegendre : GaussianQuadratureTable() {
        override val list: List<GaussianLegendreCoefficients>
            get() = listOf(
                GaussianLegendreCoefficients(0.5688888888888889, 0.0000000000000000),
                GaussianLegendreCoefficients(0.4786286704993665, -0.5384693101056831),
                GaussianLegendreCoefficients(0.4786286704993665, 0.5384693101056831),
                GaussianLegendreCoefficients(0.2369268850561891, -0.9061798459386640),
                GaussianLegendreCoefficients(0.2369268850561891, 0.9061798459386640)
            )
    }

    class GaussianQuadrature(
        curve: Parametric,
        table: GaussianQuadratureTable
    ) {
        var length = 0.0
            private set

        init {
            for(coefficient in table.list) {
                val t = 0.5 * (1.0 + coefficient.abscissa) // used for converting from [-1,1] to [0,1]
                length += curve[t, 1].norm * coefficient.weight
            }
            length *= 0.5
        }
    }

    open class Spline(
        x: DifferentiableFunction,
        y: DifferentiableFunction
    ) : Parametric(x, y) {
        private var _length: Double = 0.0
        override val length: Double
            get() = _length

        init {
            val gaussianQ = GaussianQuadrature(this, FivePointGaussianLegendre())
            _length = gaussianQ.length
        }
    }

    class SplineWithHeading(
        x: DifferentiableFunction,
        y: DifferentiableFunction
    ) : Spline(x, y) {
        fun heading(t: Double, n: Int): Double {
            return when(n) {
                0 -> this[t, 0].angle
                else -> this[t, n].cross(this[t, n+1])
            }
        }
    }

    class Path(
        private val splines: List<SplineWithHeading>
    ) {
        val length: Double = splines.sumOf { it.length }

        fun s
    }
}













