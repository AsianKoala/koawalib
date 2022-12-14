package com.asiankoala.koawalib.control.profile.v2

import com.asiankoala.koawalib.util.Clock
import kotlin.math.min
import kotlin.math.sqrt

// credit to henopied
class OnlineProfile(
    startState: DispState,
    endState: DispState,
    private val constraints: Constraints
) {
    private val dispOffset = startState.x
    private var multiplier = if (endState.x > startState.x) 1.0 else -1.0
    private val end = DispState(dispMap(endState.x), endState.v * multiplier)
    private var lastVel = startState.v * multiplier
    private var lastTime: Double? = null

    private fun dispMap(x: Double) = multiplier * (x - dispOffset)

    /**
     * @param x displacement along motion profile (RELATIVE TO START)
     * basically we want to do a forward pass (sprunk 2008) on our vels
     * the velocity planning points are:
     * 1. achievable vel from last vel (using constraint)
     * 2. vel to reach end of profile
     * 3. vel from user
     */
    private fun internalGet(x: Double): DispState {
        // guarantee we have a realistic dt
        return lastTime?.let {
            val dt = Clock.seconds - it
            val achievableVel = lastVel + constraints.accel * dt
            // vf^2 = vi^2 + 2as -> vi = sqrt(vf^2 - 2as)
            // reverse displacement so function is defined
            val ds = min(0.0, x - end.x)
            val endOfProfileVel = sqrt(end.v * end.v - 2.0 * constraints.accel * ds)
            val userVel = constraints.vel
            // now run a forward pass to find the limiting vel
            val constrainedVel = minOf(achievableVel, endOfProfileVel, userVel)
            val constrainedAccel = (constrainedVel - lastVel) / dt
            lastVel = constrainedVel
            lastTime = Clock.seconds
            DispState(
                x * multiplier + dispOffset,
                constrainedVel * multiplier,
                constrainedAccel * multiplier
            )
        } ?: run {
            lastTime = Clock.seconds
            DispState()
        }
    }

    operator fun get(x: Double): DispState {
        return internalGet(dispMap(x))
    }
}