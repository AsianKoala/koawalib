package com.asiankoala.koawalib.logger

import com.asiankoala.koawalib.util.internal.NanoClock

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

    companion object {
        operator fun get(meow: Double): Double {
            return 1.0
        }
    }
}

fun main() {

    print(TimeInterval[1.0])
}