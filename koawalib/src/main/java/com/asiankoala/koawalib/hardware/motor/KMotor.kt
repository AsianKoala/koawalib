package com.asiankoala.koawalib.hardware.motor

import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.math.MathUtil.epsilonNotEqual
import com.asiankoala.koawalib.util.KDouble
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.util.Range
import kotlin.math.absoluteValue

open class KMotor(name: String) : KDevice<DcMotorEx>(name), KDouble {

    private var offset = 0.0
    private var encoderMultiplier = 1.0

    fun zero(newPosition: Double = 0.0): KMotor {
        offset = newPosition - device.currentPosition
        return this
    }

    open fun setSpeed(speed: Double) {
        power = speed
    }

    var power: Double = 0.0
        private set(value) {
            val clipped = Range.clip(value, -1.0, 1.0)
            if (clipped epsilonNotEqual field && (clipped == 0.0 || clipped.absoluteValue == 1.0 || (clipped - field).absoluteValue > 0.005)) {
                field = value
                device.power = value
            }
        }

    var zeroPowerBehavior: DcMotor.ZeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
        set(value) {
            if (device.zeroPowerBehavior != value) {
                device.zeroPowerBehavior = value
                field = value
            }
        }

    var direction: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD
        set(value) {
            if (device.direction != value) {
                device.direction = value
                field = value
            }
        }

    val position get() = encoderMultiplier * (device.currentPosition + offset)

    val velocity get() = device.velocity

    val brake: KMotor
        get() {
            zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            return this
        }

    val float: KMotor
        get() {
            zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
            return this
        }

    val forward: KMotor
        get() {
            direction = DcMotorSimple.Direction.FORWARD
            return this
        }

    val reverse: KMotor
        get() {
            direction = DcMotorSimple.Direction.REVERSE
            return this
        }

    val resetEncoder: KMotor
        get() {
            zero()
            return this
        }

    val reverseEncoder: KMotor
        get() {
            encoderMultiplier *= -1.0
            return this
        }

    override fun invokeDouble(): Double {
        return power
    }

    init {
        device.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        device.zeroPowerBehavior = zeroPowerBehavior
        device.direction = direction
    }
}
