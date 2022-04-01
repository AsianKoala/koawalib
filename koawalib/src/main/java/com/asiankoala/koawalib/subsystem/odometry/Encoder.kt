package com.asiankoala.koawalib.subsystem.odometry

import com.acmerobotics.roadrunner.util.NanoClock
import com.asiankoala.koawalib.hardware.motor.KMotor
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign

class Encoder(
    private val motor: KMotor,
    private val ticksPerUnit: Double,
    private val isRevEncoder: Boolean = false
) {
    private var clock = NanoClock.system()
    private var offset = 0.0
    private var encoderMultiplier = 1.0
    private var _position = 0.0
    private var _velocity = 0.0
    private val prevEncoderPositions = ArrayList<Pair<Double, Double>>()

    val position get() = (_position - offset) / ticksPerUnit
    val velocity get() = _velocity / ticksPerUnit

    val delta get() = prevEncoderPositions[max(0,prevEncoderPositions.size-1)].second -
            prevEncoderPositions[max(0,prevEncoderPositions.size-2)].second

    val reversed: Encoder
        get() {
            encoderMultiplier *= -1.0
            return this
        }

    private fun attemptVelUpdate() {
        if(prevEncoderPositions.size < 2) {
            _velocity = 0.0
            return
        }

        val oldIndex = max(0, prevEncoderPositions.size - LOOK_BEHIND - 1)
        val oldPosition = prevEncoderPositions[oldIndex]
        val currPosition = prevEncoderPositions[prevEncoderPositions.size - 1]
        val scalar = (currPosition.first - oldPosition.first)
        _velocity = (currPosition.second - oldPosition.second) / scalar

        if(isRevEncoder) {
            _velocity = inverseOverflow(motor.getRawMotorVelocity * encoderMultiplier, _velocity)
        }
    }

    fun zero(newPosition: Double = 0.0): Encoder {
        offset = newPosition - motor.getRawMotorPosition
        return this
    }

    fun update() {
        _position = motor.getRawMotorPosition * encoderMultiplier
        attemptVelUpdate()
        prevEncoderPositions.add(Pair(clock.seconds(), _position))
    }

    companion object {
        private const val LOOK_BEHIND = 1
        private const val CPS_STEP = 0x10000

        private fun inverseOverflow(input: Double, estimate: Double): Double {
            var real = input
            while (abs(estimate - real) > CPS_STEP / 2.0) {
                real += sign(estimate - real) * CPS_STEP
            }
            return real
        }
    }
}
