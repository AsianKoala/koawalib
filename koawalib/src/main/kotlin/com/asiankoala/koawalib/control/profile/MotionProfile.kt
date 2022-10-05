package com.asiankoala.koawalib.control.profile

import com.asiankoala.koawalib.math.absMin
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Asymmetric trapezoidal motion profile
 * @param startState start state of profile
 * @param endState end state of profile
 * @param constraints constraints the profile must obey
 */
class MotionProfile(vararg val periods: MotionPeriod) {
    val startState: MotionState
    val endState: MotionState
    val duration: Double

    operator fun get(t: Double): MotionState {
        var dt = 0.0
        for (period in periods) {
            if (t <= period.dt + dt) return period[t - dt]
            dt += period.dt
        }
        return endState
    }

    init {
        duration = periods.sumOf { it.dt }
        startState = periods[0].startState
        endState = periods[periods.size-1].endState
    }

    companion object {
        fun generateFromConstraints(startState: MotionState, endState: MotionState, constraints: MotionConstraints): MotionProfile {
            var accelPeriod = MotionPeriod(
                startState.copy(a = constraints.accel),
                ((constraints.cruiseVel - startState.v) / constraints.accel).absoluteValue
            )

            val deccelTime = (endState.v - constraints.cruiseVel).absoluteValue / constraints.cruiseVel
            val deccelDx = endState.v * -deccelTime + 0.5 * constraints.deccel * deccelTime.pow(2)

            val cruisePeriod = MotionPeriod(
                accelPeriod.endState.copy(v = constraints.cruiseVel, a = 0.0),
                (endState.x - startState.x - accelPeriod.dx - deccelDx) / constraints.cruiseVel
            )

            var deccelPeriod = MotionPeriod(
                cruisePeriod.endState.copy(a = -constraints.deccel),
                deccelTime
            )

            if (cruisePeriod.dt < 0.0) {
                cruisePeriod.dt = 0.0
                accelPeriod = MotionPeriod(
                    accelPeriod.startState.copy(a = absMin(accelPeriod.startState.a, deccelPeriod.startState.a)),
                    sqrt(endState.x.absoluteValue / accelPeriod.startState.a)
                )
                deccelPeriod = accelPeriod.copy(startState = accelPeriod.endState.copy(a = -accelPeriod.startState.a))
            }

            return MotionProfile(accelPeriod, cruisePeriod, deccelPeriod)
        }
    }
}

