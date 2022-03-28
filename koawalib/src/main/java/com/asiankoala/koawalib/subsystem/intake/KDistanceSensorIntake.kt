package com.asiankoala.koawalib.subsystem.intake

import com.asiankoala.koawalib.hardware.motor.KMotor
import com.asiankoala.koawalib.hardware.sensor.KDistanceSensor

@Suppress("unused")
open class KDistanceSensorIntake(
    motor: KMotor,
    private val distanceSensor: KDistanceSensor,
    config: IntakeConfig,
    private val distanceSensorThreshold: Double
) : KIntake(motor, config) {

    var isMineralIn = false
        private set
    private var lastRead = Double.NaN
    private var reading = false

    fun startReading() {
        reading = true
    }

    fun stopReading() {
        reading = false
    }

    override fun periodic() {
        if (reading) {
            lastRead = distanceSensor.invokeDouble()
            isMineralIn = lastRead < distanceSensorThreshold
        }
    }
}
