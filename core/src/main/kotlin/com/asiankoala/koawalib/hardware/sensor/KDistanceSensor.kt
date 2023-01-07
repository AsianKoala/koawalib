package com.asiankoala.koawalib.hardware.sensor

import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.util.Periodic
import com.qualcomm.robotcore.hardware.DistanceSensor
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit

/**
 * DistanceSensor wrapper. Can be applied to anything that implements [DistanceSensor]
 * @param[dtms] time between reads
 */
@Suppress("unused")
class KDistanceSensor(
    name: String,
    private val dtms: Double = 50.0
) : KDevice<DistanceSensor>(name), Periodic {
    private val timer = ElapsedTime()

    var lastRead = Double.NaN
        private set

    override fun periodic() {
        if (timer.milliseconds() > dtms) {
            lastRead = device.getDistance(DistanceUnit.MM)
            timer.reset()
        }
    }
}
