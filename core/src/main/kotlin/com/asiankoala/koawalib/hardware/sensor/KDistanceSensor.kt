package com.asiankoala.koawalib.hardware.sensor

import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.util.Clock
import com.asiankoala.koawalib.util.Periodic
import com.qualcomm.robotcore.hardware.DistanceSensor
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit

/**
 * applies to the rev color sensor v3 and rev 2m distance sensor (any devices that implement DistanceSensor)
 */
@Suppress("unused")
class KDistanceSensor(
    name: String,
    private val dt: Double = 50.0
) : KDevice<DistanceSensor>(name), Periodic {
    private var lastReadTime = Clock.milliseconds

    var lastRead = Double.NaN
        private set

    override fun periodic() {
        val t = Clock.milliseconds
        if (t - lastReadTime > dt) {
            lastRead = device.getDistance(DistanceUnit.MM)
            lastReadTime = t
        }
    }
}
