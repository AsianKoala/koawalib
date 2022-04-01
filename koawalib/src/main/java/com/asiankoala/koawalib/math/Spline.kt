package com.asiankoala.koawalib.math

import kotlin.math.abs
import kotlin.math.pow

class Spline(private val polynomialDegree: Int, private val coeffs: DoubleArray, private val inverted: Boolean) : ParametricFunction {
    constructor(polynomialDegree: Int, inverted: Boolean) : this(polynomialDegree, DoubleArray(2 * (polynomialDegree+1)), inverted)
    constructor(polynomialDegree: Int) : this(polynomialDegree, false)

    enum class Axis(val offset: Int) {
        X(0), Y(1);
    }

    fun getCoefficient(order: Int, axis: Axis): Double {
        return coeffs[2 * order + axis.offset]
    }

    private fun factorial(value: Int, result: Int): Int {
        return if (value <= 1) result else factorial(value - 1, result * (value - 1))
    }

    private fun factorial(value: Int): Int {
        return if (value <= 1) 1 else factorial(value, value)
    }

    override fun evaluate(t: Double): Point {
        var vector = Point()
        for (i in 0..polynomialDegree) {
            vector = vector.plus(
                Point(coeffs[2 * i], coeffs[2 * i + 1])
                    .scale(
                        (if (inverted) 1 - t else t.pow(i.toDouble())) / factorial(i)
                    )
            )
        }

        return vector
    }

    override fun getDerivative(t: Double): Point {
        var vector = Point()
        for (i in 1..polynomialDegree) {
            vector = vector.plus(
                Point(coeffs[2 * i], coeffs[2 * i + 1])
                    .scale(
                        (if (inverted) -1.0 else 1.0) * (if (inverted) 1 - t else t.pow(
                            (i - 1).toDouble()
                        )) / factorial(i - 1)
                    )
            )
        }

        return vector
    }

    override fun getSecondDerivative(t: Double): Point {
        var vector = Point()
        for (i in 2..polynomialDegree) {
            vector = vector.plus(
                Point(coeffs[2 * i], coeffs[2 * i + 1])
                    .scale(
                        (if (inverted) 1 - t else t.pow((i - 2).toDouble())) / factorial(i - 2)
                    )
            )
        }

        return vector

    }

    fun getThirdDerivative(t: Double): Point {
        var vector = Point()
        for (i in 3..polynomialDegree) {
            vector = vector.plus(
                Point(coeffs[2 * i], coeffs[2 * i + 1])
                    .scale(
                        (if (inverted) -1.0 else 1.0) * (if (inverted) 1 - t else t.pow(
                            (i - 3).toDouble()
                        )) / factorial(i - 3)
                    )
            )
        }
        return vector
    }


    override fun getCurvature(t: Double): Double {
        val derivative = getDerivative(t)
        val secondDerivative = getSecondDerivative(t)
        return abs(derivative.x * secondDerivative.y - secondDerivative.x * derivative.y) / derivative.norm()
            .pow(3.0)
    }

    override fun getDCurvature(t: Double): Double {
        val derivative = getDerivative(t)
        val secondDerivative = getSecondDerivative(t)
        val thirdDerivative = getThirdDerivative(t)
        return abs(
            (6.0 * (derivative.y * secondDerivative.x - secondDerivative.y * derivative.x)
                    * (derivative.x * secondDerivative.x + derivative.y * secondDerivative.y)) +
                    2.0 * derivative.sqNorm() * (derivative.x * thirdDerivative.y - thirdDerivative.x * derivative.y)
        ) / (2.0 * derivative.norm().pow(5.0))
    }

    override val meanCurvature: Double
        get() = DoubleArray(T_STEPS) { (getCurvature(it.d / T_STEPS) + getCurvature((it.d + 1) / T_STEPS)) / (2.0 * T_STEPS)}.sum()

    override val meanDCurvature: Double
        get() = DoubleArray(T_STEPS) { (getDCurvature(it.d / T_STEPS) + getDCurvature((it.d + 1) / T_STEPS)) / (2.0 * T_STEPS)}.sum()

    val arcLength: Double
        get() = DoubleArray(T_STEPS) {
            (getDerivative(it.d / T_STEPS).norm() +
                    getDerivative((it.d + 1) / T_STEPS).norm()) / (2.0 * T_STEPS) }.sum()

    fun getMinDistanceFromPoint(point: Point): Double {
        return DoubleArray(T_STEPS) { evaluate(it.d / T_STEPS).dist(point) }.minOrNull()!!
    }

    fun getTAndMinDistanceFromPoint(point: Point): Pair<Double, Double> {
        var minT = -1.0
        var minDistance = Double.MAX_VALUE
        for(i in 0..T_STEPS) {
            val t = i.d / T_STEPS
            val dist = evaluate(t).dist(point)

            if(dist < minDistance) {
                minDistance = dist
                minT = t
            }
        }
        return Pair(minT, minDistance)
    }

    fun getTAtPoint(point: Point): Double {
        return getTAndMinDistanceFromPoint(point).first
    }

    companion object {
        private const val T_STEPS = 2000
    }

    init {
        assert(polynomialDegree == coeffs.size / 2 - 1)
    }
}