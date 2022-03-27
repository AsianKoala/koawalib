package com.asiankoala.koawalib.control.motion

import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.sqrt

class MotionProfile(
    startState: MotionState,
    private val endState: MotionState,
    constraints: MotionConstraints
) {

    private var accelTime: Double
    private var cruiseTime: Double
    private var deccelTime: Double
    private var accelState: MotionState
    private var cruiseState: MotionState
    private var deccelState: MotionState

    private val profileDuration: Double
    private val totalIntegral: Double

    operator fun get(time: Double): MotionState {
        return when {
            time <= accelTime -> accelState.calculate(time)
            time <= accelTime + cruiseTime -> cruiseState.calculate(time - accelTime)
            time <= profileDuration -> deccelState.calculate(time - accelTime - cruiseTime)
            else -> endState
        }
    }

    init {
        accelTime = ((constraints.vMax - startState.v) / constraints.aMax).absoluteValue
        deccelTime = ((endState.v - constraints.vMax) / constraints.dMax).absoluteValue
        accelState = MotionState(startState.x, startState.v, constraints.aMax)
        deccelState = MotionState(endState.x, endState.v, -constraints.dMax)

        val accelEndState = accelState.calculate(accelTime)
        cruiseState = MotionState(accelEndState.x, accelEndState.v, 0.0)
        val deltaX = endState.x - startState.x
        cruiseTime =
            (deltaX - accelState.integrate(accelTime) - deccelState.integrate(deccelTime)) / constraints.vMax

        if (cruiseTime < constraints.minCruiseTime) {
            cruiseTime = constraints.minCruiseTime
            constraints.aMax = max(constraints.aMax, constraints.dMax)
            constraints.dMax = constraints.aMax
            accelTime = sqrt(endState.x.absoluteValue / constraints.aMax.absoluteValue)
            deccelTime = sqrt(endState.x.absoluteValue / constraints.dMax.absoluteValue)
            val newAccelEndState = accelState.calculate(accelTime)
            deccelState = MotionState(newAccelEndState.x, newAccelEndState.v, -constraints.dMax)
        }

        profileDuration = accelTime + cruiseTime + deccelTime
        totalIntegral = accelState.integrate(accelTime) + cruiseState.integrate(cruiseTime) +
                deccelState.integrate(deccelTime)
    }
}