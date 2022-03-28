package com.asiankoala.koawalib.hardware.servo

import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.math.*
import com.qualcomm.robotcore.hardware.CRServo

@Suppress("unused")
class KCRServo(name: String) : KDevice<CRServo>(name) {

    var speed: Double = 0.0
        set(value) {
            if (field epsilonNotEqual value) {
                device.power = value
                field = value
            }
        }
}
