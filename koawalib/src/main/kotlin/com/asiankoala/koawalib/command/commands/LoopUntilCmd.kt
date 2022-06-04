package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.subsystem.Subsystem

@Suppress("unused")
open class LoopUntilCmd(
    private val action: () -> Unit,
    private val endCondition: () -> Boolean,
    vararg requirements: Subsystem
) : Cmd() {
    override fun execute() {
        action.invoke()
    }

    override val isFinished: Boolean
        get() = endCondition.invoke()

    init {
        addRequirements(*requirements)
    }
}
