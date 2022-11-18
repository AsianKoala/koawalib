package com.asiankoala.koawalib.hardware.motor

import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.estimateDerivative
import com.asiankoala.koawalib.util.Clock
import com.qualcomm.robotcore.util.MovingStatistics
import kotlin.math.abs
import kotlin.math.sign

// @Suppress("unused")
class KEncoder(
    private val motor: KMotor,
    private val ticksPerUnit: Double,
    private val isRevEncoder: Boolean = false
) {
    private var offset = 0.0
    private var multiplier = 1.0
    private var _pos = 0.0
    private var _vel = 0.0
    private var _accel = 0.0
    private val prevPos = ArrayList<Pair<Double, Double>>()
    private val prevVel = ArrayList<Pair<Double, Double>>()
    private val velStats = MovingStatistics(5)
    private val accelStats = MovingStatistics(5)
    private var disabled = false

    val pos get() = (_pos + offset) / ticksPerUnit

    val vel get() = velStats.mean / ticksPerUnit

    val accel get() = accelStats.mean / ticksPerUnit

    val delta get() = (
        prevPos[prevPos.size - 1].second -
            prevPos[prevPos.size - 2].second
        ) / ticksPerUnit

    val reverse: KEncoder
        get() {
            multiplier *= -1.0
            Logger.logInfo("encoder associated with ${motor.deviceName} reversed")
            return this
        }

    private fun attemptVelUpdate() {
        val ret = estimateDerivative(prevPos)
        if (!ret.second) return
        var estimatedVel = ret.first

        if (isRevEncoder) {
            estimatedVel = inverseOverflow(motor.rawMotorVelocity * multiplier, _vel)
        }

        velStats.add(estimatedVel)
    }

    private fun attemptAccelUpdate() {
        val ret = estimateDerivative(prevVel)
        if (!ret.second) return
        val estimatedAccel = ret.first
        accelStats.add(estimatedAccel)
    }

    private fun internalReset() {
        _pos = motor.rawMotorPosition * multiplier
        prevPos.clear()
        prevPos.add(Pair(Clock.seconds, _pos))
        prevPos.add(Pair(Clock.seconds - 1e6, _pos))
        prevVel.clear()
        velStats.clear()
        accelStats.clear()
        _vel = 0.0
        _accel = 0.0
    }

    internal fun update() {
        if (!disabled) {
            val seconds = Clock.seconds
            _pos = motor.rawMotorPosition * multiplier
            prevPos.add(Pair(seconds, _pos))
            attemptVelUpdate()
            prevVel.add(Pair(seconds, _vel))
            attemptAccelUpdate()
        } else {
            Logger.logWarning("encoder queried when disabled")
        }
    }

    fun zero(newPosition: Double = 0.0): KEncoder {
        internalReset()
        offset = newPosition * ticksPerUnit - _pos
        return this
    }

    fun disable() {
        disabled = true
    }

    fun enable() {
        disabled = false
        internalReset()
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
