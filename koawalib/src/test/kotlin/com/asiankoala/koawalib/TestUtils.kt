package com.asiankoala.koawalib

import com.asiankoala.koawalib.command.KScheduler
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.logger.LoggerConfig
import com.asiankoala.koawalib.math.EPSILON
import kotlin.test.assertEquals

internal fun assert(x: Double, y: Double) = assertEquals(x, y, absoluteTolerance = EPSILON)
internal fun assert(p: Pair<Double, Double>) = assert(p.first, p.second)
internal fun assert(xs: List<Double>, ys: List<Double>) {
    assertEquals(xs.size, ys.size)
    xs.zip(ys).forEach { assert(it) }
}

internal fun testReset() {
    Logger.reset()
    Logger.config = LoggerConfig.PRINT_CONFIG
    println()
    Logger.logDebug("~~~~~~~~~~start of test~~~~~~~~~~")
    KScheduler.resetScheduler()
}

internal fun testPeriodic(t: Int = 10) {
    for (i in 0 until t) {
        KScheduler.update()
        Logger.update()
    }
}
