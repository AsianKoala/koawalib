package com.asiankoala.koawalib.control.motion

import com.asiankoala.koawalib.math.absMin
import kotlin.math.absoluteValue
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

    private var accelPeriod: MotionPeriod
    private var cruisePeriod: MotionPeriod
    private var deccelPeriod: MotionPeriod
    private val periods: List<MotionPeriod>

    val duration: Double

    operator fun get(t: Double): MotionState {
        var dt = 0.0
        for(period in periods) {
            if(t <= period.dt + dt) return period[t - dt]
            dt += period.dt
        }
        return endState
    }

    init {
        accelPeriod = MotionPeriod(startState.copy(a = constraints.accel),
            ((constraints.cruiseVel - startState.v) / constraints.accel).absoluteValue)
        deccelPeriod = MotionPeriod(endState.copy(v = constraints.cruiseVel, a = -constraints.deccel),
            ((endState.v - constraints.cruiseVel) / constraints.deccel).absoluteValue)
        cruisePeriod = MotionPeriod(
            accelPeriod.endState.copy(a = 0.0),
            (endState.x / constraints.cruiseVel).absoluteValue - (accelPeriod.dt + deccelPeriod.dt) / 2.0
        )
        deccelPeriod.startState.x = cruisePeriod.endState.x

        if(cruisePeriod.dt < 0.0) {
            cruisePeriod.dt = 0.0
            accelPeriod = MotionPeriod(accelPeriod.startState.copy(a = absMin(accelPeriod.startState.a, deccelPeriod.startState.a)),
                sqrt(endState.x.absoluteValue / accelPeriod.startState.a))
            deccelPeriod = accelPeriod.copy(startState = accelPeriod.endState.copy(a = -accelPeriod.startState.a))
        }

        duration = accelPeriod.dt + cruisePeriod.dt + deccelPeriod.dt
        periods = listOf(accelPeriod, cruisePeriod, deccelPeriod)
    }
}
