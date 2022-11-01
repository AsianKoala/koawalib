package com.asiankoala.koawalib.logger

import android.util.Log
import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.asiankoala.koawalib.command.commands.LoopCmd
import com.asiankoala.koawalib.logger.Logger.config
import com.asiankoala.koawalib.util.internal.Colors
import org.firstinspires.ftc.robotcore.external.Telemetry
import java.text.SimpleDateFormat
import java.util.*

/**
 * Logger sends log reports to logcat detailing details of a running opmode. Also serves to format driver station telemetry
 * @property config Logger Config
 */
@Suppress("unused")
object Logger {
    var config = LoggerConfig.SIMPLE_CONFIG

    internal var telemetry: Telemetry? = null
    internal var logCount = 0; private set
    internal val priorityList = listOf("NONE", "NONE", "VERBOSE", "DEBUG", "INFO", "WARN", "ERROR", "WTF")

    private const val tag = "KOAWALIB"
    private val dashboard = FtcDashboard.getInstance()
    private val toLog = ArrayList<LogData>()
    private var packet = TelemetryPacket()
    private var warnings = 0

    private fun log(message: String, priority: Int) {
        if (!config.isLogging) return
        toLog.add(LogData(message, priority))
    }

    internal fun reset() {
        logCount = 0
        warnings = 0
        toLog.clear()
        packet = TelemetryPacket()
        config = LoggerConfig.SIMPLE_CONFIG
    }

    internal fun update() {
        if (!config.isLogging) return
        toLog.forEach {
            logCount++
            Log.println(it.priority, tag, it.formattedMessage)
        }

        toLog.clear()

        if (config.isDashboardEnabled) {
            dashboard.sendTelemetryPacket(packet)
            packet = TelemetryPacket()
        }
    }

    internal fun addWarningCountCommand() {
        + LoopCmd({ addTelemetryData("warning count", warnings) }).withName("warning counter")
    }

    fun addVar(name: String, data: Any?) {
        if (!config.isDashboardEnabled) return
        packet.put(name, data)
    }

    /**
     * Add telemetry line to phone. If config.isLoggingTelemetry, it will log the message as a debug
     * ******NOTE***** that this ignores isLogging
     * @param message string to add
     */
    fun addTelemetryLine(message: String) {
        if (!config.isTelemetryEnabled) return
        if (telemetry == null) return
        telemetry!!.addLine(message)
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
        if (!config.isLogging) return
        if (!config.isDebugging) return
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

    // note to neil: use logInfo on init stuff since that runs only once (obviously)
    // logInfo shouldn't be used on repeated stuff e.g. anything that is in main loop
    /**
     * Sends an info message to logger
     * @param message logger message to send
     */
    fun logInfo(message: String) {
        if (!config.isLogging) return
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
        if (!config.isLogging) return
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

    private fun getDataString(message: String, data: Any?): String {
        return "$message : $data"
    }

    private data class LogData(
        val message: String,
        val priority: Int,
    ) {
        private val dateString = Calendar.getInstance().time.format("HH:mm:ss.SSS")

        val formattedMessage get() = "%-10s %s".format(logCount, message)
        val printString
            get() = "$dateString \t ${priorityList[priority]} \t $formattedMessage".withColor(
                when (priority) {
                    Log.DEBUG -> Colors.ANSI_CYAN
                    Log.INFO -> Colors.ANSI_GREEN
                    else -> Colors.ANSI_PURPLE
                }
            )

        private fun Date.format(format: String, locale: Locale = Locale.getDefault()): String {
            val formatter = SimpleDateFormat(format, locale)
            return formatter.format(this)
        }

        private fun String.withColor(color: String): String {
            return "$color$this${Colors.ANSI_RESET}"
        }
    }
}
