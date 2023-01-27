package com.asiankoala.koawalib.hardware.sensor

import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.util.Clock
import com.asiankoala.koawalib.util.Periodic
import com.qualcomm.robotcore.hardware.DistanceSensor
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit

/**
 * DistanceSensor wrapper. Can be applied to anything that implements [DistanceSensor]
 * @param[dtms] time between reads
 */
@Suppress("unused")
class KDistanceSensor @JvmOverloads constructor(
    name: String,
    private val dtms: Double = 50.0
) : KDevice<DistanceSensor>(name), Periodic {
    private var lastReadTime = Clock.milliseconds
    var lastRead = Double.NaN; private set

    override fun periodic() {
        if (Clock.milliseconds - lastReadTime > dtms) {
            lastRead = device.getDistance(DistanceUnit.MM)
            lastReadTime = Clock.milliseconds
        }
    }
}
