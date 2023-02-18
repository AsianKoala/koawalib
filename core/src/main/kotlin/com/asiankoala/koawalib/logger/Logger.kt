package com.asiankoala.koawalib.logger

import android.util.Log
import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.asiankoala.koawalib.command.commands.LoopCmd
import com.asiankoala.koawalib.logger.Logger.config
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.math.radians
import com.asiankoala.koawalib.path.Waypoint
import com.asiankoala.koawalib.util.Colors
import org.firstinspires.ftc.robotcore.external.Telemetry
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Logger sends log reports to logcat detailing details of a running opmode. Also serves to format driver station telemetry
 * @property config Logger Config
 */
@Suppress("unused")
object Logger {
    @JvmStatic var config = LoggerConfig.SIMPLE_CONFIG

    internal lateinit var telemetry: Telemetry
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
        + LoopCmd({ put("warning count", warnings) }).withName("warning counter")
    }

    @JvmStatic
    fun drawRobot(p: Pose) {
        val robotRadius = 13.5 / 2.0
        val pose = Pose(p.y, -p.x, (p.heading - 90.0.radians).angleWrap)
        packet.fieldOverlay().strokeCircle(pose.x, pose.y, robotRadius)
        val (x, y) = Vector(cos(pose.heading), sin(pose.heading)).times(robotRadius)
        val x1 = pose.x + x / 2
        val y1 = pose.y + y / 2
        val x2 = pose.x + x
        val y2 = pose.y + y
        packet.fieldOverlay().strokeLine(x1, y1, x2, y2)
    }

    @JvmStatic
    fun drawPath(waypoints: List<Waypoint>) {
        val xPoints = DoubleArray(waypoints.size)
        val yPoints = DoubleArray(waypoints.size)
        for (i in waypoints.indices) {
            xPoints[i] = waypoints[i].vec.x
            yPoints[i] = waypoints[i].vec.y
        }
        packet.fieldOverlay().setStroke("red").setStrokeWidth(1).strokePolyline(xPoints, yPoints)
    }


    /**
     * Add telemetry line to phone. If config.isLoggingTelemetry, it will log the message as a debug
     * @param message string to add
     */
    @JvmStatic
    fun put(message: String = "") {
        if (config.isTelemetryEnabled) {
            telemetry.addLine(message)
            if (config.isDashboardEnabled) packet.addLine(message)
        }
    }

    /**
     * Syntax sugar for [put]
     * @param[message] caption of data
     * @param[data] data to add
     */
    @JvmStatic
    fun put(message: String, data: Any?) {
        if (config.isTelemetryEnabled) {
            telemetry.addLine(getDataString(message, data))
            if (config.isDashboardEnabled) packet.put(message, data)
        }
    }

    /**
     * Send a debug message to logger
     * @param[message] logger message to send
     */
    @JvmStatic
    fun logDebug(message: String) {
        if (config.isLogging && config.isDebugging) log(message, Log.DEBUG)
    }

    /**
     * Syntax sugar for [logDebug]
     * @param[message] caption of data
     * @param[data] data to add
     */
    @JvmStatic
    fun logDebug(message: String, data: Any?) {
        logDebug(getDataString(message, data))
    }

    /**
     * Sends an info message to logger
     * @param[message] logger message to send
     */
    @JvmStatic
    fun logInfo(message: String) {
        if (config.isLogging) log(message, Log.INFO)
    }

    /**
     * Syntax sugar for [logInfo]
     * @param[message] caption of data
     * @param[data] data to add
     */
    @JvmStatic
    fun logInfo(message: String, data: Any?) {
        logInfo(getDataString(message, data))
    }

    /**
     * Sends a warning message to logger
     * @param[message] logger message to send
     */
    @JvmStatic
    fun logWarning(message: String) {
        if (config.isLogging) {
            warnings++
            log("WARNING: $message", Log.WARN)
        }
    }

    /**
     * Syntax sugar for [logWarning]
     * @param[message] caption of data
     * @param[data] data to add
     */
    @JvmStatic
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
