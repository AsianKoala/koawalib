package com.asiankoala.koawalib.hardware.sensor

import com.asiankoala.koawalib.control.filter.RateLimiter
import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.util.Periodic
import com.qualcomm.robotcore.hardware.DistanceSensor
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit

/**
 * applies to the rev color sensor v3 and rev 2m distance sensor (any devices that implement DistanceSensor)
 */
@Suppress("unused")
class KDistanceSensor(name: String) : KDevice<DistanceSensor>(name), Periodic {
    private var rateLimiter = RateLimiter(50.0) { lastRead = device.getDistance(DistanceUnit.MM) }

    var lastRead = Double.NaN
        private set

    override fun periodic() = rateLimiter.periodic()
}
