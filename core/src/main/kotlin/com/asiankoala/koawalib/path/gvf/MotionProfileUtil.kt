package com.asiankoala.koawalib.path.gvf

import com.asiankoala.koawalib.util.Clock
import kotlin.math.sqrt

// monotonically increasing, starting at 0
data class DispState(
    val x: Double = 0.0,
    val v: Double = 0.0,
    val a: Double = 0.0
) {
    operator fun get(dx: Double) = DispState(
        x + dx,
        sqrt(v * v + 2.0 * a * dx),
        a
    )
}

data class DispPeriod(
    val start: DispState,
    val dx: Double
) {
    val end = start[dx]
    operator fun get(dx: Double) = start[dx]
}

// note: continuity is not assumed here
class DispProfile(
    vararg val periods: DispPeriod
) {
    operator fun get(x: Double): DispState {
        if(x < periods[0].start.x) return periods[0].start
        if(x > periods.last().end.x) return periods.last().end
        periods.fold(0.0) { acc, dispPeriod ->
            if(acc + dispPeriod.dx > x) return dispPeriod[x - acc]
            acc + dispPeriod.dx
        }
        throw Exception("couldn't find disp state")
    }
    
    init {
        require(periods[0].start.x < periods.last().end.x)
    }
}

data class Constraints(val vel: Double, val accel: Double)

// credit to henopied for this
class OnlineProfile(
    start: DispState,
    private val end: DispState,
    private val constraints: Constraints
) {
    private var lastVel = start.v // this is always 0.0 cause we force profiles to start 0.0
    private var lastTime = Clock.seconds

    /**
     * @param x absolute position along motion profile (NOT RELATIVE TO START)
     * basically we want to do a forward pass (sprunk 2008)
     * ur velocity planning points are:
     * 1. achievable vel from last vel (using constraint)
     * 2. vel to reach end of profile
     * 3. vel from user
     */
    operator fun get(x: Double): DispState {
        val dt = Clock.seconds - lastTime
        val achievableVel = lastVel * constraints.accel * dt
        // vf^2 = vi^2 + 2as
        // sqrt(vf^2 - 2as) = vi
        val endOfProfileVel = sqrt(end.v * end.v - 2.0 * constraints.accel * (end.x - x))
        val userVel = constraints.vel
        // now run a forward pass to find the limiting vel
        // described in diagram (b) of 3.2 of sprunk paper
        val constrainedVel = minOf(achievableVel, endOfProfileVel, userVel)
        val constrainedAccel = (constrainedVel - lastVel) / dt
        lastVel = constrainedVel
        lastTime = Clock.seconds
        return DispState(x, constrainedVel, constrainedAccel)
    }
}

data class PlanningPoint(
    val x: Double,
    val v: Double,
    val a: Double? = null
)

fun forwardPass(planningPoints: List<PlanningPoint>): List<PlanningPoint> {
    require(planningPoints[0].a != null)
    var currConstraint = planningPoints[0]
    val newPoints = mutableListOf<PlanningPoint>()
    newPoints.add(planningPoints[0])
    for(point in planningPoints.slice(1 .. planningPoints.size)) {
        if(point.a == null) {
            val ds = point.x - currConstraint.x
            val v2 = currConstraint.v * currConstraint.v
            val newV = sqrt(v2 + 2.0 * currConstraint.a!! * ds)
            newPoints.add(PlanningPoint(point.x, newV))
        } else {
            currConstraint = point
            newPoints.add(point)
        }
    }
    return newPoints
}

fun backwardPass(planningPoint: List<PlanningPoint>) =
    forwardPass(planningPoint.reversed()).reversed()
