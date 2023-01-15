package com.asiankoala.koawalib.hardware.servo

import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.math.epsilonNotEqual
import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.DcMotorSimple

/**
 * Standard CRServo wrapper
 * @param[name] hardware configuration name
 */
class KCRServo(name: String) : KDevice<CRServo>(name) {
    /**
     * power the CRServo is running at
     */
    var power: Double = 0.0
        set(value) {
            if (field epsilonNotEqual value) {
                device.power = value
                field = value
            }
        }

    fun reverse(): KCRServo {
        device.direction = DcMotorSimple.Direction.REVERSE
    }
}
