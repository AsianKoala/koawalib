package com.asiankoala.koawalib.util

import android.util.Log
import com.asiankoala.koawalib.command.commands.InfiniteCommand
import com.asiankoala.koawalib.util.Logger.config
import org.firstinspires.ftc.robotcore.external.Telemetry

/**
 * condense multiple of the same messages into a X times
 * eg: sequential command group ran X times
 * @property config Logger Config
 */
@Suppress("unused")
object Logger {
    private data class LogData(
        val message: String,
        val priority: Int,
    ) {
        var condenseCount = 0
        var updatedThisLoop = false
        val formattedMessage get() = "%-10s %s".format(condenseCount, message)
        val printString
            get() = "${priorityList[priority]} \t $message".withColor(
                when(priority) {
                    Log.DEBUG -> Colors.ANSI_CYAN
                    Log.INFO -> Colors.ANSI_GREEN
                    else -> Colors.ANSI_PURPLE
                }
            )
    }

    var config = LoggerConfig()
    internal var telemetry: Telemetry? = null
    internal var logCount = 0
    private val priorityList = listOf("NONE", "NONE", "VERBOSE", "DEBUG", "INFO", "WARN", "ERROR", "WTF")
    private var errors = 0
    private var warnings = 0
    private var condenseMap = HashMap<String, LogData>()
    private val tag = "KOAWALIB"

    internal fun update() {
        val iterator = condenseMap.iterator()

        if(errors > config.maxErrorCount) {
            logError("error overflow")
        }

        if(warnings > config.maxWarningCount) {
            logWTF("warning overflow")
        }

        while(iterator.hasNext()) {
            val data = iterator.next().value
            if(!data.updatedThisLoop) {
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

    private fun log(message: String, priority: Int) {
        if(message in condenseMap.keys) {
            condenseMap[message]!!.updatedThisLoop = true
        } else {
            condenseMap[message] = LogData(message, priority)
        }
    }

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
        warnings++
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
        logWTF(getDataString(message, data))
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
