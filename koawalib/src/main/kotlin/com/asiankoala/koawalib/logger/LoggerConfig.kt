package com.asiankoala.koawalib.logger

/**
 * Configures the logger
 * @param isLogging logging any messages
 * @param isPrinting system.print log messages
 * @param isLoggingTelemetry log ftc sdk telemetry messages
 * @param isDebugging show debug logs
 * @param maxErrorCount errors until opmode shuts off
 * @param maxWarningCount warnings until bad stuff happens
 */
data class LoggerConfig(
    var isLogging: Boolean = true,
    var isPrinting: Boolean = false,
    var isDebugging: Boolean = true,
    var isDashboardEnabled: Boolean = false,
    var isTelemetryEnabled: Boolean = true,
//    var maxErrorCount: Int = 1,
//    var maxWarningCount: Int = 1,
) {
    companion object {
        val PRINT_CONFIG = LoggerConfig(
            isLogging = true,
            isPrinting = true,
            isDebugging = true,
            isDashboardEnabled = false,
            isTelemetryEnabled = false,
        )

        val DEBUG_CONFIG = LoggerConfig(
            isLogging = true,
            isPrinting = false,
            isDebugging = true,
            isDashboardEnabled = true,
            isTelemetryEnabled = true
        )

        val COMP_CONFIG = LoggerConfig(
            isLogging = false,
            isPrinting = false,
            isDebugging = false,
            isDashboardEnabled = false,
            isTelemetryEnabled = true,
        )

        val DASHBOARD_CONFIG = LoggerConfig(
            isLogging = true,
            isPrinting = false,
            isDebugging = false,
            isDashboardEnabled = true,
            isTelemetryEnabled = true
        )
    }
}