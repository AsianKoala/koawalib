package com.asiankoala.koawalib.logger

/**
 * Configures the logger
 * @param isLogging logging any messages
 * @param isPrinting system.print log messages
 * @param isLoggingTelemetry log ftc sdk telemetry messages
 * @param isDebugging show debug logs
 * @param maxErrorCount errors until opmode shuts off
 * @param maxWarningCount warnings until bad stuff happens TODO FIX [maxWarningCount]
 */
data class LoggerConfig(
    var isLogging: Boolean = true,
    var isPrinting: Boolean = false,
    var isLoggingTelemetry: Boolean = false,
    var isDebugging: Boolean = true,
    var maxErrorCount: Int = 1,
    var maxWarningCount: Int = 1
)
