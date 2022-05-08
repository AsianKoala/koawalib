package com.asiankoala.koawalib.hardware.sensor

import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.util.Periodic
import com.asiankoala.koawalib.util.RateLimit
import com.qualcomm.robotcore.hardware.DistanceSensor
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit

/**
 * applies to the rev color sensor v3 and rev 2m distance sensor (any devices that implement DistanceSensor)
 */
@Suppress("unused")
class KDistanceSensor(name: String) : KDevice<DistanceSensor>(name), Periodic {
    private var rateLimiter = RateLimit(50.0)

    var rateLimit
        get() = rateLimiter.rateLimitMs
        set(value) {
            rateLimiter.rateLimitMs = value
        }

    var lastRead = Double.NaN
        private set

    override fun periodic() {
        if(rateLimiter.isSafeToUpdate()) {
            lastRead = device.getDistance(DistanceUnit.MM)
        }
    }
}
