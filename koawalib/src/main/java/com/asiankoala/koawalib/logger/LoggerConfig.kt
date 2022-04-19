package com.asiankoala.koawalib.logger

data class LoggerConfig(
    var isLogging: Boolean = true,
    var isPrinting: Boolean = false,
    var isLoggingTelemetry: Boolean = false,
    var isDebugging: Boolean = true,
    var maxErrorCount: Int = 1,
    var maxWarningCount: Int = 1
)