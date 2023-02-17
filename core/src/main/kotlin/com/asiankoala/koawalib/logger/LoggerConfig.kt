package com.asiankoala.koawalib.logger

/**
 * Configuration for the Logger. Includes
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
        /**
         * For debagging. Enables every option in the config.
         */
        val DEBUG_CONFIG = LoggerConfig(
            isLogging = true,
            isDebugging = true,
            isDashboardEnabled = true,
            isTelemetryEnabled = true
        )

        /**
         * For competition. Disabled all logging/telemetry.
         */
        val COMP_CONFIG = LoggerConfig(
            isLogging = false,
            isDebugging = false,
            isDashboardEnabled = false,
            isTelemetryEnabled = true,
        )

        /**
         * For FTCDashboard usage.
         */
        val DASHBOARD_CONFIG = LoggerConfig(
            isLogging = true,
            isDebugging = false,
            isDashboardEnabled = true,
            isTelemetryEnabled = true
        )

        /**
         * Simple config. This is the default configuration.
         */
        val SIMPLE_CONFIG = LoggerConfig(
            isLogging = true,
            isDebugging = false,
            isDashboardEnabled = false,
            isTelemetryEnabled = true
        )
    }
}
