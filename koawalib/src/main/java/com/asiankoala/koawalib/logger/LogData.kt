package com.asiankoala.koawalib.logger

import android.util.Log
import com.asiankoala.koawalib.util.Colors

internal data class LogData(
    val message: String,
    val priority: Int,
) {
    var condenseCount = 0
    var updatedThisLoop = false
    val formattedMessage get() = "%-10s %s".format(condenseCount, message)
    val printString
        get() = "${Logger.priorityList[priority]} \t $message".withColor(
            when(priority) {
                Log.DEBUG -> Colors.ANSI_CYAN
                Log.INFO -> Colors.ANSI_GREEN
                else -> Colors.ANSI_PURPLE
            }
        )

    private fun String.withColor(color: String): String {
        return "$color$this$Colors.ANSI_RESET"
    }
}