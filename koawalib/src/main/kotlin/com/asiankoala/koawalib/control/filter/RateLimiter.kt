package com.asiankoala.koawalib.control.filter

import com.asiankoala.koawalib.math.d
import com.asiankoala.koawalib.util.Periodic
import com.qualcomm.robotcore.util.MovingStatistics

@Suppress("unused")
class RateLimiter(private val timestepMS: Double, private val func: () -> Unit) : Periodic {
    private var lastDiscreteTime = System.currentTimeMillis()
    private var dtStats = MovingStatistics(10)

    override fun periodic() {
        val sec = System.currentTimeMillis()
        val dt = sec - lastDiscreteTime
        dtStats.add(dt.d)
        if (sec - lastDiscreteTime > timestepMS || dtStats.mean > 1.5 * timestepMS) {
            func.invoke()
            lastDiscreteTime = sec
        }
    }
}
