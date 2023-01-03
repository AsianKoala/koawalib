package com.asiankoala.koawalib.hardware.servo

import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.math.epsilonNotEqual
import com.qualcomm.robotcore.hardware.Servo

/**
 * koawalib servo device
 * @property position set/get position of servo
 * @property direction direction of servo
 * @property reverse builder to reverse servo
 */
@Suppress("unused")
class KServo(name: String) : KDevice<Servo>(name) {
    var position: Double = -1.0
        set(value) {
            if (value epsilonNotEqual field) {
                device.position = value
                field = value
            }
        }
    private var direction: Servo.Direction = Servo.Direction.FORWARD

    val reverse: KServo
        get() {
            direction = Servo.Direction.REVERSE
            return this
        }

    /**
     * Builder function to set the initialize position of the servo
     */
    fun startAt(startPos: Double): KServo {
        position = startPos
        return this
    }
}
