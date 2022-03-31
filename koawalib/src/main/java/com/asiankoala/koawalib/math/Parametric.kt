package com.asiankoala.koawalib.math

interface ParametricFunction {
    fun evaluate(parameter: Double): Point
    fun getDerivative(parameter: Double): Point
    fun getSecondDerivative(parameter: Double): Point
    fun getCurvature(parameter: Double): Double
    fun getDCurvature(parameter: Double): Double
    val meanCurvature: Double
    val meanDCurvature: Double
}
