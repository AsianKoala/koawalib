package com.asiankoala.koawalib.control.motion

import com.asiankoala.koawalib.math.absMin
import kotlin.math.absoluteValue

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

    val duration: Double

    operator fun get(t: Double): MotionState {
        return when {
            t <= accelPeriod.dt -> accelPeriod[t]
            t <= accelPeriod.dt + cruisePeriod.dt -> cruisePeriod[t - accelPeriod.dt]
            t <= duration -> deccelPeriod[t - accelPeriod.dt - cruisePeriod.dt]
            else -> endState
        }
    }

    init {
        accelPeriod = MotionPeriod(startState.copy(a = constraints.accel),
            ((constraints.cruiseVel - startState.v) / constraints.accel).absoluteValue)
        deccelPeriod = MotionPeriod(endState.copy(v = constraints.cruiseVel, a = constraints.deccel),
            ((endState.v - constraints.cruiseVel) / constraints.deccel).absoluteValue)
        cruisePeriod = MotionPeriod(
            accelPeriod.endState.copy(a = 0.0),
            (deccelPeriod.startState.x - accelPeriod.endState.x) / constraints.cruiseVel
        )

        if(cruisePeriod.dt < 0.0) {
            cruisePeriod.dt = 0.0
            accelPeriod.startState.a = absMin(accelPeriod.startState.a, deccelPeriod.startState.a)
            deccelPeriod.startState.a = -accelPeriod.startState.a
//            val accelDist =
        }

        duration = accelPeriod.dt + cruisePeriod.dt + deccelPeriod.dt
    }
}
