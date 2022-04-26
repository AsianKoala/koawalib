package com.asiankoala.koawalib.wpilib.filter

import com.acmerobotics.roadrunner.util.NanoClock
import com.asiankoala.koawalib.math.clamp

/**
 * @see https://github.com/wpilibsuite/allwpilib/blob/main/wpimath/src/main/java/edu/wpi/first/math/filter/SlewRateLimiter.java
 * A class that limits the rate of change of an input value. Useful for implementing voltage,
 * setpoint, and/or output ramps. A slew-rate limit is most appropriate when the quantity being
 * controlled is a velocity or a voltage; when controlling a position, consider using a [ ] instead.
 */
class SlewRateLimiter @JvmOverloads constructor(
    private val m_rateLimit: Double,
    private var m_prevVal: Double = 0.0
) {
    private var m_prevTime: Double = NanoClock.system().seconds()

    /**
     * Filters the input to limit its slew rate.
     *
     * @param input The input value whose slew rate is to be limited.
     * @return The filtered value, which will not change faster than the slew rate.
     */
    fun calculate(input: Double): Double {
        val currentTime: Double = NanoClock.system().seconds()
        val elapsedTime = currentTime - m_prevTime
        m_prevVal += clamp(
            input - m_prevVal,
            -m_rateLimit * elapsedTime,
            m_rateLimit * elapsedTime
        )
        m_prevTime = currentTime
        return m_prevVal
    }

    /**
     * Resets the slew rate limiter to the specified value; ignores the rate limit when doing so.
     *
     * @param value The value to reset to.
     */
    fun reset(value: Double) {
        m_prevVal = value
        m_prevTime = NanoClock.system().seconds()
    }
    /**
     * Creates a new SlewRateLimiter with the given rate limit and initial value.
     *
     * @param m_rateLimit The rate-of-change limit, in units per second.
     * @param m_prevVal The initial value of the input.
     */
    /**
     * Creates a new SlewRateLimiter with the given rate limit and an initial value of zero.
     *
     * @param rateLimit The rate-of-change limit, in units per second.
     */
}