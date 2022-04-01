package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.hardware.motor.KMotor
import kotlin.math.max

class Encoder(private val positionSupplier: () -> Double, private val ticksPerUnit: Double) {
    constructor(motorSupplier: KMotor, ticksPerUnit: Double) : this({ motorSupplier.getRawMotorPosition }, ticksPerUnit)

    private var offset = 0.0
    private var encoderMultiplier = 1.0
    private var _position = 0.0
    private var _velocity = 0.0
    private val prevEncoderPositions = ArrayList<Pair<Double, Double>>()

    val position get() = (_position - offset) / ticksPerUnit
    val velocity get() = _velocity / ticksPerUnit

    val delta get() = prevEncoderPositions[max(0,prevEncoderPositions.size-1)].second -
            prevEncoderPositions[max(0,prevEncoderPositions.size-2)].second

    private fun attemptVelUpdate() {
        if(prevEncoderPositions.size < 2) {
            _velocity = 0.0
        }

        val oldIndex = max(0, prevEncoderPositions.size - 5)
        val oldPosition = prevEncoderPositions[oldIndex]
        val currPosition = prevEncoderPositions[prevEncoderPositions.size - 1]
        val scalar = (currPosition.first - oldPosition.first) / 1000.0
        _velocity = (currPosition.second - oldPosition.second) / scalar
    }

    val reversed: Encoder
        get() {
            encoderMultiplier *= -1.0
            return this
        }

    fun zero(newPosition: Double = 0.0): Encoder {
        offset = newPosition - positionSupplier.invoke()
        return this
    }

    fun update() {
        _position = encoderMultiplier * positionSupplier.invoke()
        prevEncoderPositions.add(Pair(System.currentTimeMillis().toDouble(), _position))
        attemptVelUpdate()
    }
}
