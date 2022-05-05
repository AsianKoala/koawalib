package com.asiankoala.koawalib.rework

import com.acmerobotics.roadrunner.util.NanoClock
import com.asiankoala.koawalib.math.clamp
import com.asiankoala.koawalib.math.epsilonNotEqual
import com.asiankoala.koawalib.math.inputModulus
import kotlin.math.absoluteValue

/**
 * ENCODER IS NOT AUTO UPDATED IN THIS CLASS
 */
@Suppress("unused")
class PIDController(
    var kP: Double,
    var kI: Double,
    var kD: Double,
) {
    private val clock = NanoClock.system()
    private var minIntegral = -1.0
    private var maxIntegral = 1.0
    private var isContinuous = false
    private var minInput = Double.NaN
    private var maxInput = Double.NaN

    private var prevTime = Double.NaN
    private var positionError = 0.0
    private var velocityError = 0.0
    private var prevError = 0.0
    private var sumError = 0.0
    var position = Double.NaN
        private set

    var target = Double.NaN

    private fun updateError() {
        prevError = positionError

        positionError = if (isContinuous) {
            val errorBound = (maxInput - minInput) / 2.0
            inputModulus(target - position, -errorBound, errorBound)
        } else {
            target - position
        }
    }

    fun isAtTarget(positionEpsilon: Double, velocityEpsilon: Double = Double.POSITIVE_INFINITY): Boolean {
        return positionError.absoluteValue < positionEpsilon && velocityError.absoluteValue < velocityEpsilon
    }

    fun enableContinuousInput(min: Double, max: Double) {
        isContinuous = true
        minInput = min
        maxInput = max
    }

    fun disableContinuousInput() {
        isContinuous = false
        minInput = Double.NaN
        maxInput = Double.NaN
    }

    fun setIntegratorRange(minInt: Double, maxInt: Double) {
        minIntegral = minInt
        maxIntegral = maxInt
    }

    fun update(measured: Double): Double {
        position = measured
        updateError()
        return if (prevTime.isNaN()) {
            prevTime = clock.seconds()
            0.0
        } else {
            val dt = clock.seconds() - prevTime

            velocityError = (positionError - prevError) / dt

            if (kI epsilonNotEqual 0.0) {
                sumError = clamp(
                    sumError + positionError * dt,
                    minIntegral / kI,
                    maxIntegral / kI
                )
            }

            kP * positionError + kI * sumError + kD * velocityError
        }
    }

    fun reset() {
        sumError = 0.0
        prevError = 0.0
    }
}
