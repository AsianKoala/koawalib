package com.asiankoala.koawalib.command.commands

import android.util.Log

@Suppress("unused")
class LogCommand(message: String, priority: Int) : InstantCommand({ Log.println(priority, "KOAWALIB", message) })