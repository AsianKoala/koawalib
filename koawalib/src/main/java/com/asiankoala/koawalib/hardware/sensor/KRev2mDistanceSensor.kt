package com.asiankoala.koawalib.hardware.sensor

import com.asiankoala.koawalib.hardware.HardwareDevice
import com.asiankoala.koawalib.util.KDouble
import com.qualcomm.hardware.rev.Rev2mDistanceSensor
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit

class KRev2mDistanceSensor : HardwareDevice<Rev2mDistanceSensor>, KDouble {
    constructor(device: Rev2mDistanceSensor) : super(device)
    constructor(name: String) : super(name)

    override fun invokeDouble(): Double {
        return device.getDistance(DistanceUnit.MM)
    }
}
