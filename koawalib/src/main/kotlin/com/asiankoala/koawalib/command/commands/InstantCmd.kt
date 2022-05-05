package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.subsystem.Subsystem

/**
 * Command that runs once and stops
 * @param action action to run
 * @param requirements subsystem requirements
 */
open class InstantCmd(
    private val action: () -> Unit,
    vararg requirements: Subsystem
) : Cmd() {

    override fun execute() {
        action.invoke()
    }

    override val isFinished: Boolean = true

    init {
        addRequirements(*requirements)
    }
}
