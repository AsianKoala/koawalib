package com.asiankoala.koawalib.control.motion

import com.asiankoala.koawalib.math.absMax
import com.asiankoala.koawalib.math.absMin
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Asymmetric trapezoidal motion profile
 * @param startState start state of profile
 * @param endState end state of profile
 * @param constraints constraints the profile must obey
 */
// TODO: test if works on real robot
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

    val profileDuration: Double
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

        if (cruiseTime < 0) {
            cruiseTime = 0.0
            constraints.aMax = absMin(constraints.aMax, constraints.dMax)
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
