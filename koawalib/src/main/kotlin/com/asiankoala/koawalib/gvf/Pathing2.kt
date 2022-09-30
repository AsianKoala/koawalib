package com.asiankoala.koawalib.gvf

import com.asiankoala.koawalib.math.Vector
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
    abstract class Parametric(
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

    class GaussianQuadrature(
        curve: Parametric,
        vararg table: GaussianLegendreCoefficients
    ) {
        var length = 0.0
            private set

        init {
            for(coefficient in table) {
                val t = 0.5 * (1.0 + coefficient.abscissa) // used for converting from [-1,1] to [0,1]
                length += curve[t][1].norm * coefficient.weight
            }
            length *= 0.5
        }
    }

}