package com.asiankoala.koawalib.logger

import com.asiankoala.koawalib.util.Clock

class TimeInterval {
    private var start = 0.0
    private var sum = 0.0
    private var its = 0

    fun start() {
        start = Clock.milliseconds
    }

    fun end() {
        val dt = Clock.milliseconds - start
        sum += dt
        its += 1
    }

    val avgMs get() = sum / its
}
