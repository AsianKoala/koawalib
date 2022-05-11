package com.asiankoala.koawalib.control

import com.acmerobotics.roadrunner.util.NanoClock
import kotlin.math.abs
import kotlin.math.sign

/**
 * ENCODER IS NOT AUTO UPDATED IN THIS CLASS
 */
@Suppress("unused")
class PIDController(
    var kP: Double,
    var kI: Double,
    var kD: Double,
) {
    private var clock = NanoClock.system()
    private var errorSum: Double = 0.0
    private var lastUpdateTimestamp: Double = Double.NaN

    private var inputBounded: Boolean = false
    private var minInput: Double = 0.0
    private var maxInput: Double = 0.0

    private var outputBounded: Boolean = false
    private var minOutput: Double = 0.0
    private var maxOutput: Double = 0.0

    /**
     * Target position (that is, the controller setpoint).
     */
    var targetPosition: Double = 0.0

    /**
     * Target velocity.
     */
    var targetVelocity: Double = 0.0

    /**
     * Target acceleration.
     */
    var targetAcceleration: Double = 0.0

    /**
     * Error computed in the last call to [update].
     */
    var lastError: Double = 0.0
        private set

    /**
     * Sets bound on the input of the controller. The min and max values are considered modularly-equivalent (that is,
     * the input wraps around).
     *
     * @param min minimum input
     * @param max maximum input
     */
    fun setInputBounds(min: Double, max: Double) {
        if (min < max) {
            inputBounded = true
            minInput = min
            maxInput = max
        }
    }

    /**
     * Sets bounds on the output of the controller.
     *
     * @param min minimum output
     * @param max maximum output
     */
    fun setOutputBounds(min: Double, max: Double) {
        if (min < max) {
            outputBounded = true
            minOutput = min
            maxOutput = max
        }
    }

    private fun getPositionError(measuredPosition: Double): Double {
        var error = targetPosition - measuredPosition
        if (inputBounded) {
            val inputRange = maxInput - minInput
            while (abs(error) > inputRange / 2.0) {
                error -= sign(error) * inputRange
            }
        }
        return error
    }

    @JvmOverloads
    fun update(
        measuredPosition: Double,
        measuredVelocity: Double? = null
    ): Double {
        val currentTimestamp = clock.seconds()
        val error = getPositionError(measuredPosition)
        return if (lastUpdateTimestamp.isNaN()) {
            lastError = error
            lastUpdateTimestamp = currentTimestamp
            0.0
        } else {
            val dt = currentTimestamp - lastUpdateTimestamp
            errorSum += 0.5 * (error + lastError) * dt
            val errorDeriv = (error - lastError) / dt

            lastError = error
            lastUpdateTimestamp = currentTimestamp

            kP * error + kI * errorSum +
                kD * (measuredVelocity?.let { targetVelocity - it } ?: errorDeriv)
        }
    }

    /**
     * Reset the controller's integral sum.
     */
    fun reset() {
        errorSum = 0.0
        lastError = 0.0
        lastUpdateTimestamp = Double.NaN
    }
}
