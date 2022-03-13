package com.asiankoala.koawalib.command.commands

open class PrintCommand(message: String) : InstantCommand({ println(message) }) {
    companion object {
        val priorityList = listOf("NONE", "NONE", "VERBOSE", "DEBUG", "INFO", "WARN", "ERROR", "WTF")
    }
}