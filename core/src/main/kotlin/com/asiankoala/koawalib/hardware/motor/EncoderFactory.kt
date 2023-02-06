package com.asiankoala.koawalib.hardware.motor

import com.asiankoala.koawalib.util.internal.cond

class EncoderFactory(
    private val ticksPerUnit: Double
) {
    private var zeroPos = 0.0
    private var isReversed = false
    private var isRevEncoder = false

    val reverse: EncoderFactory
        get() {
            isReversed = true
            return this
        }

    val revEncoder: EncoderFactory
        get() {
            isRevEncoder = true
            return this
        }

    @JvmOverloads
    fun zero(x: Double = 0.0): EncoderFactory {
        zeroPos = x
        return this
    }


    fun build(motor: KMotor): KEncoder {
        return KEncoder(
            motor,
            ticksPerUnit,
            isRevEncoder
        ).cond(isReversed) { it.reverse }.zero(zeroPos)
    }
}

