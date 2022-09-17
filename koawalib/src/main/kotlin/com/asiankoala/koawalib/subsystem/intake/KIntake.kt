package com.asiankoala.koawalib.subsystem.intake

import com.asiankoala.koawalib.hardware.motor.KMotor
import com.asiankoala.koawalib.subsystem.Subsystem

@Suppress("unused")
open class KIntake(private val motor: KMotor, private val MAX_POWER: Double) : Subsystem() {

    fun turnOn() {
        motor.power = MAX_POWER
    }

    fun turnReverse() {
        motor.power = -MAX_POWER
    }

    fun turnOff() {
        motor.power = 0.0
    }

    fun setIntakeSpeed(speed: Double) {
        motor.power = speed
    }
}
