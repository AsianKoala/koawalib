package com.asiankoala.koawalib.hardware.sensor

import com.asiankoala.koawalib.hardware.HardwareDevice
import com.asiankoala.koawalib.util.KDouble
import com.qualcomm.hardware.rev.RevColorSensorV3
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit

class KRevColorSensor : HardwareDevice<RevColorSensorV3>, KDouble {
    constructor(device: RevColorSensorV3) : super(device)
    constructor(name: String) : super(name)

    override fun invokeDouble(): Double {
        return device.getDistance(DistanceUnit.MM)
    }
}
