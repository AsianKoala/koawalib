package com.asiankoala.koawalib.hardware.sensor

import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.util.KDouble
import com.qualcomm.robotcore.hardware.DistanceSensor
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit

/**
 * applies to the rev color sensor v3 and rev 2m distance sensor (any devices that implement DistanceSensor)
 */
@Suppress("unused")
class KDistanceSensor(name: String) : KDevice<DistanceSensor>(name), KDouble {
    private var _lastRead = Double.POSITIVE_INFINITY
    val lastRead get() = _lastRead
    override fun invokeDouble(): Double {
        _lastRead = device.getDistance(DistanceUnit.MM)
        return lastRead
    }
}
