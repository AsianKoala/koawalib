package com.asiankoala.koawalib.questionmark

import com.asiankoala.koawalib.math.Point

interface ParametricFunction {
    fun evaluate(t: Double): Point
    fun getDerivative(t: Double): Point
    fun getSecondDerivative(t: Double): Point
    fun getCurvature(t: Double): Double
    fun getDCurvature(t: Double): Double
    val meanCurvature: Double
    val meanDCurvature: Double
}
