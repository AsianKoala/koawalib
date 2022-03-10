package com.asiankoala.koawalib.hardware.servo

import com.asiankoala.koawalib.hardware.HardwareDevice
import com.asiankoala.koawalib.math.MathUtil.epsilonNotEqual
import com.qualcomm.robotcore.hardware.CRServo

@Suppress("unused")
class KCRServo(name: String) : HardwareDevice<CRServo>(name) {

    var speed: Double = 0.0
        set(value) {
            if(field epsilonNotEqual value) {
                device.power = value
                field = value
            }
        }
}