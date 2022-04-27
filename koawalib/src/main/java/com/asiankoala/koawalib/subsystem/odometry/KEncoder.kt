package com.asiankoala.koawalib.subsystem.odometry

import com.acmerobotics.roadrunner.util.NanoClock
import com.acmerobotics.roadrunner.util.epsilonEquals
import com.asiankoala.koawalib.hardware.motor.KMotor
import com.asiankoala.koawalib.logger.Logger
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign

@Suppress("unused")
class KEncoder(
    private val motor: KMotor,
    private val ticksPerUnit: Double,
    private val isRevEncoder: Boolean = false
) {
    private var clock = NanoClock.system()
    private var offset = 0.0
    private var multiplier = 1.0
    private var _pos = 0.0
    private var _vel = 0.0
    private var _accel = 0.0
    private val prevPos = ArrayList<Pair<Double, Double>>()
    private val prevVel = ArrayList<Pair<Double, Double>>()

    val pos get() = (_pos + offset) * multiplier / ticksPerUnit

    val vel get() = _vel / ticksPerUnit

    val accel get() = _accel / ticksPerUnit

    val delta get() = (
        prevPos[prevPos.size - 1].second -
            prevPos[prevPos.size - 2].second
        ) / ticksPerUnit

    val reversed: KEncoder
        get() {
            multiplier *= -1.0
            return this
        }

    private fun attemptVelUpdate() {
        if (prevPos.size < 2) {
            _vel = 0.0
            return
        }

        val oldIndex = max(0, prevPos.size - LOOK_BEHIND - 1)
        val oldPosition = prevPos[oldIndex]
        val currPosition = prevPos[prevPos.size - 1]
        val scalar = (currPosition.first - oldPosition.first)

        if (scalar epsilonEquals 0.0) Logger.logError(motor.toString())

        _vel = (currPosition.second - oldPosition.second) / scalar

        if (isRevEncoder) {
            _vel = inverseOverflow(motor.rawMotorVelocity * multiplier, _vel)
        }
    }

    private fun attemptAccelUpdate() {
        if (prevVel.size < 2) {
            _accel = 0.0
            return
        }

        val oldIndex = max(0, prevVel.size - LOOK_BEHIND - 1)
        val oldVel = prevVel[oldIndex]
        val currVel = prevVel[prevPos.size - 1]
        val scalar = (currVel.first - oldVel.first)

        if (scalar epsilonEquals 0.0) Logger.logError(motor.toString())

        _accel = (currVel.second - oldVel.second) / scalar
    }

    fun zero(newPosition: Double = 0.0): KEncoder {
        _pos = motor.rawMotorPosition
        offset = newPosition * ticksPerUnit - _pos
        prevPos.clear()
        prevPos.add(Pair(clock.seconds(), _pos))
        prevPos.add(Pair(clock.seconds() - 1e6, _pos))
        _vel = 0.0
        return this
    }

    fun update() {
        val seconds = clock.seconds()
        prevPos.add(Pair(seconds, _pos))
        _pos = motor.rawMotorPosition
        attemptVelUpdate()
        prevVel.add(Pair(seconds, _vel))
        attemptAccelUpdate()
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
