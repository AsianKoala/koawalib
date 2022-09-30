package com.asiankoala.koawalib.logger

import com.asiankoala.koawalib.util.NanoClock

class TimeInterval {
    private var start = 0.0
    private var sum = 0.0
    private var its = 0
    private val clock = NanoClock.system()

    fun start() {
        start = clock.milliseconds()
    }

    fun end() {
        val dt = clock.milliseconds() - start
        sum += dt
        its += 1
    }

    val avgMs get() = sum / its
}