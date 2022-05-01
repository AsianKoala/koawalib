package com.asiankoala.koawalib.hardware.motor

import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.math.d
import com.asiankoala.koawalib.math.epsilonNotEqual
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.util.Range
import kotlin.math.absoluteValue

/**
 * The koawalib standard open-loop motor. Default settings are zeroPowerBehavior: float and direction: forward
 * @see KMotorEx for closed-loop control
 */
open class KMotor(name: String) : KDevice<DcMotorEx>(name) {
    private var powerMultiplier = 1.0

    /**
     * raw motor position (ticks, no offset)
     */
    val rawMotorPosition get() = device.currentPosition.d

    /**
     * raw motor velocity (ticks)
     */
    val rawMotorVelocity get() = device.velocity

    var power: Double = 0.0
        set(value) {
            var clipped = Range.clip(value, -1.0, 1.0) * powerMultiplier
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
            powerMultiplier = if (value == DcMotorSimple.Direction.FORWARD) {
                1.0
            } else {
                -1.0
            }
            field = value
        }

    /**
     * Return this motor with brake mode
     */
    val brake: KMotor
        get() {
            zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            return this
        }

    /**
     * Return this motor with float mode
     */
    val float: KMotor
        get() {
            zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
            return this
        }

    /**
     * Return this motor with direction forward
     */
    val forward: KMotor
        get() {
            direction = DcMotorSimple.Direction.FORWARD
            return this
        }

    /**
     * Return this motor with direction backward
     */
    val reverse: KMotor
        get() {
            direction = DcMotorSimple.Direction.REVERSE
            return this
        }

    init {
        device.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        device.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
    }
}
