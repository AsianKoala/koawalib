package com.asiankoala.koawalib.control.controller

import com.asiankoala.koawalib.math.epsilonEquals
import com.asiankoala.koawalib.util.Clock
import kotlin.math.abs
import kotlin.math.sign

// this is the old rr PIDFController that got removed in RR2
class PIDFController constructor(
    private val pid: PIDGains,
    private val kV: Double = 0.0,
    private val kA: Double = 0.0,
    private val kStatic: Double = 0.0,
    private val kF: (Double, Double?) -> Double = { _, _ -> 0.0 },
) {
    private var errorSum: Double = 0.0
    private var lastUpdateTimestamp: Double = Double.NaN

    private var inputBounded: Boolean = false
    private var minInput: Double = 0.0
    private var maxInput: Double = 0.0

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

    var lastError: Double = 0.0
        private set

    fun setInputBounds(min: Double, max: Double) {
        if (min < max) {
            inputBounded = true
            minInput = min
            maxInput = max
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

    /**
     * Run a single iteration of the controller.
     *
     * @param measuredPosition measured position (feedback)
     * @param measuredVelocity measured velocity
     */
    @JvmOverloads
    fun update(
        measuredPosition: Double,
        measuredVelocity: Double? = null
    ): Double {
        val currentTimestamp = Clock.seconds
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

            val baseOutput = pid.kP * error + pid.kI * errorSum +
                pid.kD * (measuredVelocity?.let { targetVelocity - it } ?: errorDeriv) +
                kV * targetVelocity + kA * targetAcceleration + kF(measuredPosition, measuredVelocity)
            val output = if (baseOutput epsilonEquals 0.0) 0.0 else baseOutput + sign(baseOutput) * kStatic

//            Logger.addTelemetryData("kp", pid.kP * error)
//            Logger.addTelemetryData("error", error)
//            Logger.addTelemetryData("curr", measuredPosition)
//            Logger.addTelemetryData("target", targetPosition)

            output
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
