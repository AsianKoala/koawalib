package com.asiankoala.koawalib.control.filter

import com.acmerobotics.roadrunner.util.NanoClock
import com.asiankoala.koawalib.util.Periodic
import com.qualcomm.robotcore.util.MovingStatistics

@Suppress("unused")
class RateLimiter(private val timestepSec: Double, private val func: () -> Unit) : Periodic {
    private val clock = NanoClock.system()
    private var lastDiscreteTime = clock.seconds()
    private var dtStats = MovingStatistics(10)

    override fun periodic() {
        val sec = clock.seconds()
        val dt = sec - lastDiscreteTime
        dtStats.add(dt)
        if (sec - lastDiscreteTime > timestepSec || dtStats.mean > 1.5 * timestepSec) {
            func.invoke()
            lastDiscreteTime = sec
        }
    }
}
