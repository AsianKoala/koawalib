package com.asiankoala.koawalib.questionmark

import kotlin.math.absoluteValue
import kotlin.math.pow

class Polynomial(coefficients_: DoubleArray) {
    val coefficients = coefficients_.reversed()
    fun calculate(x: Double): Double {
        var sum = 0.0
        coefficients.forEachIndexed { index: Int, coef: Double ->
            if (index == 0) {
                sum += coef
            }
            else {
                sum += coef * x.pow(index)
            }
        }
        return sum
    }
    fun derivative(x: Double): Double {
        var sum = 0.0
        coefficients.forEachIndexed { index: Int, coef: Double ->
            if (index == 1) {
                sum += coef
            }
            else if (index > 1) {
                sum += coef * index * x.pow(index - 1)
            }

        }
        return sum
    }
    fun secondDerivative(x: Double): Double {
        var sum = 0.0
        coefficients.forEachIndexed { index: Int, coef: Double ->
            if (index == 2) {
                sum += coef
            }
            else if (index > 2) {
                sum += coef * index * (index - 1) * x.pow(index - 2)
            }
        }
        return sum
    }
    fun isLinear(): Boolean {
        return coefficients.size <= 2 || coefficients.subList(2, coefficients.size).sumByDouble { it.absoluteValue } == 0.0
    }

    override fun toString(): String {
        var str = ""
        for (i in (coefficients.size - 1) downTo 0) {
            str += coefficients[i].toString() + "x^" + i
            if (i != 0) {
                str += " + "
            }
        }
        return str
    }
}
