package com.asiankoala.koawalib.hardware.servo

import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.math.epsilonNotEqual
import com.qualcomm.robotcore.hardware.Servo

@Suppress("unused")
class KServo(name: String) : KDevice<Servo>(name) {

    var position: Double = -1.0
        set(value) {
            if (value epsilonNotEqual field) {
                device.position = value
                field = value
            }
        }

    fun startAt(startPos: Double): KServo {
        position = startPos
        return this
    }

    var direction: Servo.Direction = Servo.Direction.FORWARD
        private set

    val reverse : KServo
        get() {
            direction = Servo.Direction.REVERSE
            return this
        }
}
