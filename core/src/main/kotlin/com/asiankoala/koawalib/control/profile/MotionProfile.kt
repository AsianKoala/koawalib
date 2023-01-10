package com.asiankoala.koawalib.control.profile

import com.asiankoala.koawalib.logger.Logger
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Asymmetric trapezoidal motion profile
 * @param startState start state of profile
 * @param endState end state of profile
 * @param constraints constraints the profile must obey
 */
class MotionProfile(vararg _periods: MotionPeriod, reversed: Boolean) {
    private val periods: List<MotionPeriod>
    private val startState: MotionState
    private val endState: MotionState
    val duration: Double

    operator fun get(t: Double): MotionState {
        if (t >= duration) return endState
        if (t <= 0.0) return startState
        periods.fold(0.0) { acc, it ->
            if (t <= it.dt + acc) return it[t - acc]
            it.dt + acc
        }
        throw Exception("?")
    }

    init {
        duration = _periods.sumOf { it.dt }
        periods = if (reversed) {
            _periods.reversed().map { it.flipped }
        } else {
            _periods.toList()
        }
        startState = periods[0].startState
        endState = periods.last().endState
    }

    companion object {
        fun generateTrapezoidal(startState: MotionState, endState: MotionState, constraints: MotionConstraints): MotionProfile {
            val isReversed = endState.x < startState.x
            var start = startState
            var end = endState
            if (isReversed) {
                end = startState
                start = endState
            }

            println("reversed: $isReversed")
            println("start: $start")
            println("end: $end")

            start = start.copy(a = constraints.accel)
            end = end.copy(a = constraints.deccel)

            var accelPeriod = MotionPeriod(
                start,
                (constraints.maxV - start.v / constraints.accel).absoluteValue
            )

            val deccelTime = ((end.v - constraints.maxV) / constraints.deccel).absoluteValue
            val deccelStartState = end.copy(a = constraints.deccel)[-deccelTime]
            var deccelPeriod = MotionPeriod(deccelStartState, deccelTime)

            val dx = (end.x - start.x).absoluteValue
            val cruiseDt = (dx - accelPeriod.dx - deccelPeriod.dx) / constraints.maxV
            var cruisePeriod = MotionPeriod(accelPeriod.endState.copy(a = 0.0), cruiseDt)

            if (cruiseDt < 0.0) {
                Logger.logInfo("profile wont reach cruise vel")
                cruisePeriod = cruisePeriod.copy(dt = 0.0)
                val newA = min(constraints.accel.absoluteValue, constraints.deccel.absoluteValue)
                val dt = sqrt(dx / newA)
                accelPeriod = MotionPeriod(start.copy(a = newA), dt)
                deccelPeriod = MotionPeriod(accelPeriod.endState.copy(a = -newA), dt)
            }

            return MotionProfile(accelPeriod, cruisePeriod, deccelPeriod, reversed = isReversed)
        }
    }
}
