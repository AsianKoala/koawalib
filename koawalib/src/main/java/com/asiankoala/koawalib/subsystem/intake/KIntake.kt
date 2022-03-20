package com.asiankoala.koawalib.subsystem.intake

import com.acmerobotics.dashboard.config.Config
import com.asiankoala.koawalib.hardware.motor.KMotor
import com.asiankoala.koawalib.subsystem.DeviceSubsystem

@Suppress("unused")
open class KIntake(private val motor: KMotor, private val config: IntakeConfig): DeviceSubsystem() {

    fun turnOn() {
        motor.setSpeed(config.MAX_POWER)
    }

    fun turnReverse() {
        motor.setSpeed(-config.MAX_POWER)
    }

    fun turnOff() {
        motor.setSpeed(0.0)
    }

    fun setIntakeSpeed(speed: Double) {
        motor.setSpeed(speed)
    }
}