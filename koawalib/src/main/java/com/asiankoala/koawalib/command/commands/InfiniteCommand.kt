package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.subsystem.Subsystem

// infinitely runs this command (ends when the opmode ends)
class InfiniteCommand(
    private val action: () -> Unit = {},
    vararg requirements: Subsystem
) : CommandBase() {
    override fun execute() {
        action.invoke()
    }

    init {
        addRequirements(*requirements)
    }
}
