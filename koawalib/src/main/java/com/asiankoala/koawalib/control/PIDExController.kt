package com.asiankoala.koawalib.control

import com.acmerobotics.roadrunner.util.NanoClock
import com.acmerobotics.roadrunner.util.epsilonEquals
import kotlin.math.*

open class PIDExController(private val config: PIDFConfig) : Controller() {
    private val clock: NanoClock = NanoClock.system()

    private var errorSum: Double = 0.0
    private var lastUpdateTimestamp: Double = Double.NaN

    private var inputBounded: Boolean = false
    private var minInput: Double = 0.0
    private var maxInput: Double = 0.0

    private var outputBounded: Boolean = false
    private var minOutput: Double = 0.0
    private var maxOutput: Double = 0.0

    var lastError: Double = 0.0
        private set

    var targetPosition = 0.0
        private set
    private var targetVelocity = 0.0
    private var targetAcceleration = 0.0

    var currentPosition: Double = 0.0
        private set
    var currentVelocity: Double? = 0.0
        private set

    val isAtTarget get() = (currentPosition - targetPosition).absoluteValue < config.positionEpsilon

    val isHomed get() = !config.homePositionToDisable.isNaN() &&
        (targetPosition - config.homePositionToDisable).absoluteValue < config.positionEpsilon &&
        (config.homePositionToDisable - currentPosition).absoluteValue < config.positionEpsilon

    private fun getPositionError(measuredPosition: Double): Double {
        var error = targetPosition - measuredPosition
        if (inputBounded) {
            val inputRange = maxInput - minInput
            while (abs(error) > inputRange / 2.0) {
                error -= kotlin.math.sign(error) * inputRange
            }
        }
        return error
    }

    private fun ticksToUnits(ticks: Double): Double {
        return ticks / config.ticksPerUnit
    }

    fun reset() {
        errorSum = 0.0
        lastError = 0.0
        lastUpdateTimestamp = Double.NaN
    }

    fun measure(position: Double, velocity: Double) {
        currentPosition = ticksToUnits(position)
        currentVelocity = ticksToUnits(velocity)
    }

    fun setControllerTargets(targetP: Double, targetV: Double = 0.0, targetA: Double = 0.0) {
        targetPosition = targetP
        targetVelocity = targetV
        targetAcceleration = targetA
    }

    override fun process(): Double {
        return if (isHomed) {
            0.0
        } else {
            /**
             * Copy pasted @see roadrunner's PID controller lol
             */
            val currentTimestamp = clock.seconds()
            val error = getPositionError(currentPosition)
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

                // note: we'd like to refactor this with Kinematics.calculateMotorFeedforward() but kF complicates the
                // determination of the sign of kStatic
                val pidOutput = config.kP * error + config.kI * errorSum + config.kD * (currentVelocity?.let { targetVelocity - it } ?: errorDeriv)
                val ffOutput = config.feedforward.getFeedforward(targetPosition, targetVelocity, targetAcceleration)
                val baseOutput = pidOutput + ffOutput
                val output = if (baseOutput epsilonEquals 0.0) 0.0 else baseOutput + sign(baseOutput) * config.feedforward.kStatic

                if (outputBounded) {
                    max(minOutput, min(output, maxOutput))
                } else {
                    output
                }
            }
        }
    }
}
