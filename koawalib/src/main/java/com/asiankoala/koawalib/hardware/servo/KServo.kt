package com.asiankoala.koawalib.hardware.servo

import com.asiankoala.koawalib.hardware.HardwareDevice
import com.asiankoala.koawalib.math.MathUtil.epsilonNotEqual
import com.qualcomm.robotcore.hardware.Servo

@Suppress("unused")
class KServo(name: String) : HardwareDevice<Servo>(name) {

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
}
