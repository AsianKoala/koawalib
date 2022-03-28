package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.hardware.motor.KMotor

class Encoder(private val motor: KMotor, private val ticksPerInch: Double) {
    var offset = 0.0
        private set
    private var lastRead = 0.0
    var currRead = 0.0
        private set
        get() {
            return (field - offset) / ticksPerInch
        }

    val delta get() = currRead - lastRead

    fun read() {
        lastRead = currRead
        currRead = motor.position
    }

    init {
        offset = motor.position
    }
}
