package com.asiankoala.koawalib.util

import android.util.Log
import com.asiankoala.koawalib.command.commands.InfiniteCommand
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.robotcore.external.Telemetry

@Suppress("unused")
object Logger {
    internal var telemetry: Telemetry? = null

    private const val maxErrorCount = 100
    private const val refreshRate = 20

    private val priorityList = listOf("NONE", "NONE", "VERBOSE", "DEBUG", "INFO", "WARN", "ERROR", "WTF")

    private var logCount = 0
    private var isLogging: Boolean = true
    private var isPrinting: Boolean = false
    private var shouldLogTelemetry: Boolean = false
    private var messageCache = ArrayDeque<Pair<Int, String>>()
    private var internalTimer = ElapsedTime()
    private var forceUpdate = false
    private var errors = 0

    fun periodic() {
        if (internalTimer.milliseconds() > 1000 / refreshRate || forceUpdate) {
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
        }
        forceUpdate = false
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
            if (shouldLogTelemetry) {
                logInfo(message)
            }
        }
    }

    fun addTelemetryData(message: String, data: Any?) {
        addTelemetryLine("$message : $data")
    }

    fun logDebug(message: String) {
        log(message, Log.DEBUG)
    }

    fun logInfo(message: String) {
        log(message, Log.INFO)
    }

    fun logWarning(message: String) {
        log(message, Log.WARN)
    }

    fun logError(message: String) {
        errors++
        log(message, Log.ERROR)
    }

    fun logWTF(message: String) {
        log(message, Log.ASSERT)
        throw Exception(message)
    }

    fun startLogging() {
        isLogging = true
    }

    fun stopLogging() {
        isLogging = false
    }

    fun startPrinting() {
        isPrinting = true
    }

    fun stopPrinting() {
        isPrinting = false
    }

    fun reset() {
        logCount = 0
    }

    fun addErrorCommand() {
        InfiniteCommand({ addTelemetryData("error count", errors) }).schedule()
    }

    fun forceLoggerUpdate() {
        forceUpdate = true
    }

    private fun String.withColor(color: String): String {
        return "$color$this$Colors.ANSI_RESET"
    }

}