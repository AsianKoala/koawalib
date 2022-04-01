package com.asiankoala.koawalib.control

import com.acmerobotics.roadrunner.util.NanoClock
import com.acmerobotics.roadrunner.util.epsilonEquals
import kotlin.math.absoluteValue
import kotlin.math.sign

open class PIDExController(private val config: PIDFConfig) : Controller() {
    private val clock: NanoClock = NanoClock.system()

    private var errorSum: Double = 0.0
    private var lastUpdateTimestamp: Double = Double.NaN

    private var lastError: Double = 0.0
    private var targetPosition = 0.0
    private var targetVelocity = 0.0
    private var targetAcceleration = 0.0

    internal var currentPosition: Double = 0.0
    internal var currentVelocity: Double = 0.0

    val isAtTarget get() = (currentPosition - targetPosition).absoluteValue < config.positionEpsilon

    val isHomed get() = !config.homePositionToDisable.isNaN() &&
        (targetPosition - config.homePositionToDisable).absoluteValue < config.positionEpsilon &&
        (config.homePositionToDisable - currentPosition).absoluteValue < config.positionEpsilon

    private fun ticksToUnits(ticks: Double): Double {
        return ticks / config.ticksPerUnit
    }

    fun reset() {
        errorSum = 0.0
        lastError = 0.0
        lastUpdateTimestamp = Double.NaN
    }

    fun setControllerTargets(targetP: Double, targetV: Double = 0.0, targetA: Double = 0.0) {
//        reset()
        targetPosition = targetP
        targetVelocity = targetV
        targetAcceleration = targetA
    }

    override fun update(): Double {
        return if (isHomed) {
            0.0
        } else {
            /**
             * Copy pasted @see roadrunner's PID controller lol
             */
            val currentTimestamp = clock.seconds()
            val error = targetPosition - currentPosition
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

                val pidOutput = config.kP * error + config.kI * errorSum + config.kD * (currentVelocity?.let { targetVelocity - it } ?: errorDeriv)
                val ffOutput = config.feedforward.getFeedforward(targetPosition, targetVelocity, targetAcceleration)
                val baseOutput = pidOutput + ffOutput
                val output = if (baseOutput epsilonEquals 0.0) 0.0 else baseOutput + baseOutput.sign * config.feedforward.kStatic

                output
            }
        }
    }
}
