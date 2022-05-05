package com.asiankoala.koawalib.util

import com.acmerobotics.roadrunner.util.NanoClock
import com.qualcomm.robotcore.util.MovingStatistics

@Suppress("unused")
class Discretize(private val timestep: Double, private val func: () -> Unit) : Periodic {
    private val clock = NanoClock.system()
    private var lastDiscreteTime = clock.seconds()
    private var dtStats = MovingStatistics(10)

    override fun periodic() {
        val sec = clock.seconds()
        val dt = sec - lastDiscreteTime
        dtStats.add(dt)
        if (sec - lastDiscreteTime > timestep || dtStats.mean > 1.5 * timestep) {
            func.invoke()
            lastDiscreteTime = sec
        }
    }
}
