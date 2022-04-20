package com.asiankoala.koawalib.logger

import android.util.Log
import com.asiankoala.koawalib.command.commands.InfiniteCommand
import com.asiankoala.koawalib.logger.Logger.config
import com.asiankoala.koawalib.util.Colors
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * Logger sends log reports to logcat detailing details of a running opmode. Also serves to format driver station telemetry
 * @property config Logger Config
 */
@Suppress("unused")
object Logger {
    var config = LoggerConfig()
    internal var telemetry: Telemetry? = null
    internal var logCount = 0
    internal val priorityList = listOf("NONE", "NONE", "VERBOSE", "DEBUG", "INFO", "WARN", "ERROR", "WTF")
    private var errors = 0
    private var warnings = 0
    private var condenseMap = HashMap<String, LogData>()
    private val tag = "KOAWALIB"

    private fun log(message: String, priority: Int) {
        if(message in condenseMap.keys) {
            condenseMap[message]!!.updatedThisLoop = true
        } else {
            condenseMap[message] = LogData(message, priority)
        }
    }

    internal fun update() {
        val iterator = condenseMap.iterator()

        if(errors > config.maxErrorCount) {
            logError("error overflow")
        }

        while(iterator.hasNext()) {
            val data = iterator.next().value
            if(!data.updatedThisLoop) {
                logCount++
                Log.println(data.priority, tag, data.formattedMessage)

                if(config.isPrinting) {
                    println(data.printString)
                }

                iterator.remove()
            } else {
                data.condenseCount++
                data.updatedThisLoop = false
            }
        }
    }

    internal fun addErrorCommand() {
        InfiniteCommand({ addTelemetryData("error count", errors) }).withName("error counter").schedule()
    }

    /**
     * Add telemetry line to phone. If config.isLoggingTelemetry, it will log the message as a debug
     * @param message string to add
     */
    fun addTelemetryLine(message: String) {
        if (telemetry == null) {
            val nullStr = "LogManager telemetry is null"
            logError(nullStr)
        } else {
            telemetry!!.addLine(message)
            if (config.isLoggingTelemetry) {
                logDebug(message)
            }
        }
    }

    /**
     * Syntax sugar for [addTelemetryLine]
     * @param message caption of data
     * @param data data to add
     */
    fun addTelemetryData(message: String, data: Any?) {
        addTelemetryLine("$message : $data")
    }

    /**
     * Send a debug message to logger
     * @param message logger message to send
     */
    fun logDebug(message: String) {
        if(!config.isDebugging) return
        log(message, Log.DEBUG)
    }

    /**
     * Syntax sugar for [logDebug]
     * @param message caption of data
     * @param data data to add
     */
    fun logDebug(message: String, data: Any?) {
        logDebug(getDataString(message, data))
    }

    /**
     * Sends an info message to logger
     * @param message logger message to send
     */
    fun logInfo(message: String) {
        log(message, Log.INFO)
    }

    /**
     * Syntax sugar for [logInfo]
     * @param message caption of data
     * @param data data to add
     */
    fun logInfo(message: String, data: Any?) {
        logInfo(getDataString(message, data))
    }

    /**
     * Sends a warning message to logger
     * @param message logger message to send
     */
    fun logWarning(message: String) {
        warnings++
        log("WARNING: $message", Log.WARN)
    }

    /**
     * Syntax sugar for [logWarning]
     * @param message caption of data
     * @param data data to add
     */
    fun logWarning(message: String, data: Any?) {
        logWarning(getDataString(message, data))
    }

    /**
     * Sends an error message to Logger, and throws an exception if too many errors
     * @param message string
     */
    fun logError(message: String) {
        errors++
        log("ERROR: $message", Log.ERROR)
    }

    /**
     * Syntax sugar for [logError]
     * @param message caption of data
     * @param data data to add
     */
    fun logError(message: String, data: Any?) {
        logError(getDataString(message, data))
    }

    private fun getDataString(message: String, data: Any?): String {
        return "$message : $data"
    }
}
