package com.asiankoala.koawalib.hardware

import com.asiankoala.koawalib.math.MathUtil.epsilonNotEqual
import com.qualcomm.robotcore.hardware.Servo

class KServo : HardwareDevice<Servo> {
    constructor(device: Servo) : super(device)
    constructor(name: String) : super(name)

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
