package com.asiankoala.koawalib.command.commands

open class PrintCommand(message: String) : InstantCommand({ println(message) })