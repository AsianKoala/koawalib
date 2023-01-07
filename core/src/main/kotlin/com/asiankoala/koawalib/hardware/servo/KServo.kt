package com.asiankoala.koawalib.hardware.servo

import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.math.epsilonNotEqual
import com.qualcomm.robotcore.hardware.Servo

/**
 * Standard Servo wrapper
 * @param[name] hardware config name
 */
@Suppress("unused")
class KServo(name: String) : KDevice<Servo>(name) {
    private var direction: Servo.Direction = Servo.Direction.FORWARD
    /**
     * Servo's position
     */
    var position: Double = -1.0
        set(value) {
            if (value epsilonNotEqual field) {
                device.position = value
                field = value
            }
        }

    /**
     * Builder function to reverse the servo
     * TODO: might remove this since you can just .apply {} instead?
     */
    fun reverse(): KServo {
        direction = Servo.Direction.REVERSE
        return this
    }

    /**
     * Builder function to set the initialize position of the servo
     * TODO: might remove this since you can just .apply {} instead?
     */
    fun startAt(startPos: Double): KServo {
        position = startPos
        return this
    }
}
