package com.asiankoala.koawalib.util

import android.util.Log
import com.asiankoala.koawalib.command.commands.InfiniteCommand
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.robotcore.external.Telemetry

@Suppress("unused")
object Logger {
    internal var telemetry: Telemetry? = null

    private const val maxErrorCount = 10

    private val priorityList = listOf("NONE", "NONE", "VERBOSE", "DEBUG", "INFO", "WARN", "ERROR", "WTF")

    internal var logCount = 0
    private var errors = 0
    private var messageCache = ArrayDeque<Pair<Int, String>>()

    var isLogging: Boolean = true
    var isPrinting: Boolean = false
    var isLoggingTelemetry: Boolean = false
    var isForceUpdating = false
    var isDebugging = true

    fun periodic() {
        val tag = "KOAWALIB"
        messageCache.forEach {
            logCount++
            if (isLogging) {
                Log.println(it.first, tag, it.second)
            }

            if (isPrinting) {
                val color = when (it.first) {
                    Log.DEBUG -> Colors.ANSI_CYAN
                    Log.INFO -> Colors.ANSI_GREEN
                    else -> Colors.ANSI_PURPLE
                }

                val printMessage = "${priorityList[it.first]} \t ${it.second}"
                println(printMessage.withColor(color))
            }
        }

        if(errors > maxErrorCount) {
            throw Exception("error overflow")
        }
    }

    private fun log(message: String, priority: Int) {
        logCount++
        val formattedMessage = "%-10s %s".format(logCount, message)
        messageCache.add(Pair(priority, formattedMessage))
    }

    fun addTelemetryLine(message: String) {
        if (telemetry == null) {
            val nullStr = "LogManager telemetry is null"
            if (isPrinting) {
                logWarning(nullStr)
                logInfo(message)
            } else {
                logError(nullStr)
            }
        } else {
            telemetry!!.addLine(message)
            if (isLoggingTelemetry) {
                logInfo(message)
            }
        }
    }

    fun addTelemetryData(message: String, data: Any?) {
        addTelemetryLine("$message : $data")
    }

    fun logDebug(message: String) {
        if(isDebugging) return
        log(message, Log.DEBUG)
    }

    fun logInfo(message: String) {
        if(isDebugging) return
        log(message, Log.INFO)
    }

    fun logWarning(message: String) {
        log("WARNING: $message", Log.WARN)
    }

    fun logError(message: String) {
        errors++
        log("ERROR: $message", Log.ERROR)
    }

    fun logWTF(message: String) {
        log(message, Log.ASSERT)
        throw Exception(message)
    }

    fun addErrorCommand() {
        InfiniteCommand({ addTelemetryData("error count", errors) }).schedule()
    }

    private fun String.withColor(color: String): String {
        return "$color$this$Colors.ANSI_RESET"
    }
}
