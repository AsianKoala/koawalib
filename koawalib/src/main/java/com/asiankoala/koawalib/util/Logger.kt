package com.asiankoala.koawalib.util

import android.util.Log
import com.asiankoala.koawalib.command.commands.InfiniteCommand
import org.firstinspires.ftc.robotcore.external.Telemetry

@Suppress("unused")
object Logger {
    var config = LoggerConfig()
    internal var telemetry: Telemetry? = null
    internal var logCount = 0
    private val priorityList = listOf("NONE", "NONE", "VERBOSE", "DEBUG", "INFO", "WARN", "ERROR", "WTF")
    private var errors = 0

    private fun log(message: String, priority: Int) {
        val tag = "KOAWALIB"
        logCount++
        val formattedMessage = "%-10s %s".format(logCount, message)

        if (config.isPrinting) {
            val color = when (priority) {
                Log.DEBUG -> Colors.ANSI_CYAN
                Log.INFO -> Colors.ANSI_GREEN
                else -> Colors.ANSI_PURPLE
            }

            val printMessage = "${priorityList[priority]} \t ${message}"
            println(printMessage.withColor(color))
        }

        if (config.isLogging) {
            Log.println(priority, tag, formattedMessage)
        }

        if(errors > config.maxErrorCount) {
            throw Exception("error overflow")
        }
    }

    fun addTelemetryLine(message: String) {
        if (telemetry == null) {
            val nullStr = "LogManager telemetry is null"
            if (config.isPrinting) {
                logWarning(nullStr)
                logInfo(message)
            } else {
                logError(nullStr)
            }
        } else {
            telemetry!!.addLine(message)
            if (config.isLoggingTelemetry) {
                logInfo(message)
            }
        }
    }

    fun addTelemetryData(message: String, data: Any?) {
        addTelemetryLine("$message : $data")
    }

    fun logDebug(message: String) {
        if(!config.isDebugging) return
        log(message, Log.DEBUG)
    }

    fun logDebug(message: String, data: Any?) {
        logDebug(getDataString(message, data))
    }

    fun logInfo(message: String) {
        log(message, Log.INFO)
    }

    fun logInfo(message: String, data: Any?) {
        logInfo(getDataString(message, data))
    }

    fun logWarning(message: String) {
        log("WARNING: $message", Log.WARN)
    }

    fun logWarning(message: String, data: Any?) {
        logWarning(getDataString(message, data))
    }

    fun logError(message: String) {
        errors++
        log("ERROR: $message", Log.ERROR)
    }

    fun logError(message: String, data: Any?) {
        logError(getDataString(message, data))
    }

    fun logWTF(message: String) {
        log(message, Log.ASSERT)
        throw Exception(message)
    }

    fun logWTF(message: String, data: Any?) {
        logWarning(getDataString(message, data))
    }

    fun addErrorCommand() {
        InfiniteCommand({ addTelemetryData("error count", errors) }).withName("error counter").schedule()
    }

    private fun String.withColor(color: String): String {
        return "$color$this$Colors.ANSI_RESET"
    }

    private fun getDataString(message: String, data: Any?): String {
        return "$message : $data"
    }
}
