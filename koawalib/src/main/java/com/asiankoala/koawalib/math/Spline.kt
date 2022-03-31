package com.asiankoala.koawalib.math

import kotlin.math.pow

class Spline(val polynomialDegree: Int, val coeffs: DoubleArray, val inverted: Boolean) : ParametricFunction {
    constructor(polynomialDegree: Int, inverted: Boolean) : this(polynomialDegree, DoubleArray(2 * (polynomialDegree+1)), inverted)
    constructor(polynomialDegree: Int) : this(polynomialDegree, false)

    enum class Axis(val offset: Int) {
        X(0), Y(1);
    }

    fun getCoefficient(order: Int, axis: Axis): Double {
        return coeffs.get(2 * order + axis.offset)
    }

    private fun factorial(value: Int, result: Int): Int {
        return if (value <= 1) result else factorial(value - 1, result * (value - 1))
    }

    private fun factorial(value: Int): Int {
        return if (value <= 1) 1 else factorial(value, value)
    }

    override fun evaluate(parameter: Double): Point {
        var vector = Point()
        for (i in 0..polynomialDegree) {
            vector = vector.plus(
                Point(coeffs[2 * i], coeffs[2 * i + 1])
                    .scale(
                        (if (inverted) 1 - parameter else parameter.pow(i.toDouble())) / factorial(i)
                    )
            )
        }

        return vector
    }

    override fun getDerivative(parameter: Double): Point {
        var vector = Point()
        for (i in 1..polynomialDegree) {
            vector = vector.plus(
                Point(coeffs[2 * i], coeffs[2 * i + 1])
                    .scale(
                        (if (inverted) -1.0 else 1.0) * Math.pow(
                            if (inverted) 1 - parameter else parameter,
                            (i - 1).toDouble()
                        ) / factorial(i - 1)
                    )
            )
        }

        return vector
    }

    override fun getSecondDerivative(parameter: Double): Point {
        var vector = Point()
        for (i in 2..polynomialDegree) {
            vector = vector.plus(
                Point(coeffs[2 * i], coeffs[2 * i + 1])
                    .scale(
                        Math.pow(
                            if (inverted) 1 - parameter else parameter,
                            (i - 2).toDouble()
                        ) / factorial(i - 2)
                    )
            )
        }

        return vector

    }

    fun getThirdDerivative(parameter: Double): Point {
        var vector = Point()
        for (i in 3..polynomialDegree) {
            vector = vector.plus(
                Point(coeffs[2 * i], coeffs[2 * i + 1])
                    .scale(
                        (if (inverted) -1.0 else 1.0) * Math.pow(
                            if (inverted) 1 - parameter else parameter,
                            (i - 3).toDouble()
                        ) / factorial(i - 3)
                    )
            )
        }
        return vector
    }


    override fun getCurvature(parameter: Double): Double {
        val derivative = getDerivative(parameter)
        val secondDerivative = getSecondDerivative(parameter)
        return Math.abs(derivative.x * secondDerivative.y - secondDerivative.x * derivative.y) / Math.pow(
            derivative.norm(),
            3.0
        )
    }

    override fun getDCurvature(parameter: Double): Double {
        val derivative = getDerivative(parameter)
        val secondDerivative = getSecondDerivative(parameter)
        val thirdDerivative = getThirdDerivative(parameter)
        return Math.abs(
            (6.0 * (derivative.y * secondDerivative.x - secondDerivative.y * derivative.x)
                    * (derivative.x * secondDerivative.x + derivative.y * secondDerivative.y)) +
                    2.0 * derivative.sqNorm() * (derivative.x * thirdDerivative.y - thirdDerivative.x * derivative.y)
        ) / (2.0 * Math.pow(derivative.norm(), 5.0))
    }

    override val meanCurvature: Double
        get() = TODO("Not yet implemented")
    override val meanDCurvature: Double
        get() = TODO("Not yet implemented")


    companion object {
        private const val PARAMETER_STEPS = 2000
    }

    init {
        assert(polynomialDegree == coeffs.size / 2 - 1)
    }
}