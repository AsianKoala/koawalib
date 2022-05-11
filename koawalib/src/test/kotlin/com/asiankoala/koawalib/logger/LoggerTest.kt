package com.asiankoala.koawalib.logger

import kotlin.test.Test
import kotlin.test.assertEquals

internal class LoggerTest {
    @Test
    fun testLogger() {
        Logger.reset()
        Logger.config = LoggerConfig.PRINT_CONFIG
        Logger.logInfo("hello")
        Logger.logDebug("meow")
        Logger.update()
        assertEquals(2, Logger.logCount)
    }
}
