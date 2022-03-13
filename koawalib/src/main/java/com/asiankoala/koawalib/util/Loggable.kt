package com.asiankoala.koawalib.util

import android.util.Log
import com.asiankoala.koawalib.command.commands.LogCommand
import com.asiankoala.koawalib.command.commands.PrintCommand

@Suppress("unused")
interface Loggable {
    var logCount: Int
    var isLogging: Boolean
    var isPrinting: Boolean

    fun log(message: String, priority: Int) {
        logCount++
        val formattedMessage = "%-10s %s".format(logCount, message)
        val printMessage = "${PrintCommand.priorityList[priority]} \t $formattedMessage"

        val color = when(priority) {
            Log.DEBUG -> ANSI_CYAN
            Log.INFO -> ANSI_GREEN
            else -> ANSI_PURPLE
        }

        if(isLogging) LogCommand(formattedMessage.withColor(color), priority).execute()
        if(isPrinting) PrintCommand(printMessage.withColor(color)).execute()
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
        log(message, Log.ERROR)
    }

    fun logWTF(message: String) {
        log(message, Log.ASSERT)
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

    fun String.withColor(color: String): String {
        return "$color$this$ANSI_RESET"
    }

    companion object {
        const val ANSI_RESET = "\u001B[0m"
        const val ANSI_BLACK = "\u001B[30m"
        const val ANSI_RED = "\u001B[31m"
        const val ANSI_GREEN = "\u001B[32m"
        const val ANSI_YELLOW = "\u001B[33m"
        const val ANSI_BLUE = "\u001B[34m"
        const val ANSI_PURPLE = "\u001B[35m"
        const val ANSI_CYAN = "\u001B[36m"
        const val ANSI_WHITE = "\u001B[37m"
        const val ANSI_BLACK_BACKGROUND = "\u001B[40m"
        const val ANSI_RED_BACKGROUND = "\u001B[41m"
        const val ANSI_GREEN_BACKGROUND = "\u001B[42m"
        const val ANSI_YELLOW_BACKGROUND = "\u001B[43m"
        const val ANSI_BLUE_BACKGROUND = "\u001B[44m"
        const val ANSI_PURPLE_BACKGROUND = "\u001B[45m"
        const val ANSI_CYAN_BACKGROUND = "\u001B[46m"
        const val ANSI_WHITE_BACKGROUND = "\u001B[47m"
        const val ANSI_BOLD = "\u001B[1m"
        const val ANSI_UNBOLD = "\u001B[21m"
        const val ANSI_UNDERLINE = "\u001B[4m"
        const val ANSI_STOP_UNDERLINE = "\u001B[24m"
        const val ANSI_BLINK = "\u001B[5m"
    }
}