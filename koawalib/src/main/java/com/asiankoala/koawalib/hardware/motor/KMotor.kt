package com.asiankoala.koawalib.hardware.motor

import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.math.*
import com.asiankoala.koawalib.util.KDouble
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.util.Range
import kotlin.math.absoluteValue

open class KMotor(name: String) : KDevice<DcMotorEx>(name), KDouble {
    private var powerMultiplier = 1.0

    fun setSpeed(speed: Double) {
        this.power = speed
    }

    val getRawMotorPosition get() = device.currentPosition.d
    val getRawMotorVelocity get() = device.velocity

    private var power: Double = 0.0
        private set(value) {
            val clipped = Range.clip(value, -1.0, 1.0) * powerMultiplier
            if (clipped epsilonNotEqual field && (clipped == 0.0 || clipped.absoluteValue == 1.0 || (clipped - field).absoluteValue > 0.005)) {
                field = clipped
                device.power = clipped
            }
        }

    private var zeroPowerBehavior: DcMotor.ZeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
        set(value) {
            device.zeroPowerBehavior = value
            field = value
        }

    private var direction: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD
        set(value) {
            powerMultiplier = if(value == DcMotorSimple.Direction.FORWARD) {
                1.0
            } else {
                -1.0
            }
            field = value
        }

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

    override fun invokeDouble(): Double {
        return power
    }

    init {
        device.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        device.zeroPowerBehavior = zeroPowerBehavior
    }
}
