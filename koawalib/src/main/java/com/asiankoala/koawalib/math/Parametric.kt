package com.asiankoala.koawalib.math

interface ParametricFunction {
    fun evaluate(t: Double): Point
    fun getDerivative(t: Double): Point
    fun getSecondDerivative(t: Double): Point
    fun getCurvature(t: Double): Double
    fun getDCurvature(t: Double): Double
    val meanCurvature: Double
    val meanDCurvature: Double
}
