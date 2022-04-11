package com.asiankoala.koawalib.subsystem.intake

import com.asiankoala.koawalib.hardware.motor.KMotor
import com.asiankoala.koawalib.hardware.sensor.KDistanceSensor

open class KDistanceSensorIntake(
    motor: KMotor,
    private val sensor: KDistanceSensor,
    MAX_POWER: Double,
    private val SENSOR_THRESHOLD: Double
) : KIntake(motor, MAX_POWER) {

    private var isReadingSensor = false
    private var lastRead = Double.POSITIVE_INFINITY

    fun startReading() {
        isReadingSensor = true
        lastRead = Double.POSITIVE_INFINITY
    }

    fun stopReading() {
        isReadingSensor = false
        lastRead = Double.POSITIVE_INFINITY
    }

    val hasMineral get() = sensor.lastRead < SENSOR_THRESHOLD

    override fun periodic() {
        if(isReadingSensor) {
            lastRead = sensor.invokeDouble()
        }
    }
}
