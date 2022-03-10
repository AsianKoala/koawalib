package com.asiankoala.koawalib.hardware.sensor

import com.asiankoala.koawalib.hardware.HardwareDevice
import com.asiankoala.koawalib.util.KDouble
import com.qualcomm.hardware.rev.RevColorSensorV3
import com.qualcomm.robotcore.hardware.DistanceSensor
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit

/**
 * applies to the rev color sensor v3 and rev 2m distance sensor (any devices that implement DistanceSensor)
 */
@Suppress("unused")
class KDistanceSensor(name: String) : HardwareDevice<DistanceSensor>(name), KDouble {

    override fun invokeDouble(): Double {
        return device.getDistance(DistanceUnit.MM)
    }
}
