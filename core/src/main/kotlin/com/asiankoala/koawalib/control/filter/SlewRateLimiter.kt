package com.asiankoala.koawalib.control.filter

import com.asiankoala.koawalib.math.clamp
import com.asiankoala.koawalib.util.Clock

/**
 * A class that limits the rate of change of an input value. Useful for implementing voltage,
 * setpoint, and/or output ramps. A slew-rate limit is most appropriate when the quantity being
 * controlled is a velocity or a voltage; when controlling a position, consider using a {@link
 * package com.asiankoala.koawalib.control.profile.MotionProfile} instead.
 */
class SlewRateLimiter(
    private val r: Double,
) {
    private var ukm1 = 0.0
    private var tkm1 = Clock.seconds

    /**
     * Filters the input to limit its slew rate.
     *
     * @param input The input value whose slew rate is to be limited.
     * @return The filtered value, which will not change faster than the slew rate.
     */
    fun calculate(input: Double): Double {
        val dt = Clock.seconds - tkm1
        ukm1 += clamp(input - ukm1, -r * dt, r * dt)
        tkm1 = Clock.seconds
        return ukm1
    }

    /**
     * Resets the slew rate limiter to the specified value; ignores the rate limit when doing so.
     *
     * @param u The value to reset to.
     */
    fun reset(u: Double) {
        ukm1 = u
        tkm1 = Clock.seconds
    }
}
