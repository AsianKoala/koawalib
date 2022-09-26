package com.asiankoala.koawalib.logger

/**
 * Configures the logger
 * @param isLogging logging any messages
 * @param isDebugging show debug logs
 * @param isDashboardEnabled send data packets to FTCDashboard
 * @param isTelemetryEnabled send telemetry to driver station
 */
data class LoggerConfig(
    var isLogging: Boolean,
    var isDebugging: Boolean,
    var isDashboardEnabled: Boolean,
    var isTelemetryEnabled: Boolean,
) {
    companion object {
        // when debugging
        val DEBUG_CONFIG = LoggerConfig(
            isLogging = true,
            isDebugging = true,
            isDashboardEnabled = true,
            isTelemetryEnabled = true
        )

        // in comp
        val COMP_CONFIG = LoggerConfig(
            isLogging = false,
            isDebugging = false,
            isDashboardEnabled = false,
            isTelemetryEnabled = false,
        )

        // for FTCDashboard
        val DASHBOARD_CONFIG = LoggerConfig(
            isLogging = true,
            isDebugging = false,
            isDashboardEnabled = true,
            isTelemetryEnabled = true
        )

        // default config
        val SIMPLE_CONFIG = LoggerConfig(
            isLogging = true,
            isDebugging = false,
            isDashboardEnabled = false,
            isTelemetryEnabled = true
        )
    }
}
