package com.asiankoala.koawalib.logger

import android.util.Log
import com.asiankoala.koawalib.util.Colors
import java.text.SimpleDateFormat
import java.util.*

internal data class LogData(
    val message: String,
    val priority: Int,
) {
    private val dateString = Calendar.getInstance().time.format("HH:mm:ss.SSS")

    val formattedMessage get() = "%-10s %s".format(Logger.logCount, message)
    val printString
        get() = "$dateString \t ${Logger.priorityList[priority]} \t $formattedMessage".withColor(
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
