package com.asiankoala.koawalib.subsystem.intake

import com.acmerobotics.dashboard.config.Config
import com.asiankoala.koawalib.hardware.motor.KMotor
import com.asiankoala.koawalib.subsystem.DeviceSubsystem

open class KIntake(private val motor: KMotor, private val config: IntakeConfig): DeviceSubsystem() {

    fun turnOn() {
        motor.setSpeed(config.ON_POWER)
    }

    fun turnReverse() {
        motor.setSpeed(config.REVERSE_POWER)
    }

    fun turnOff() {
        motor.setSpeed(config.OFF_POWER)
    }

    fun setIntakeSpeed(speed: Double) {
        motor.setSpeed(speed)
    }
}