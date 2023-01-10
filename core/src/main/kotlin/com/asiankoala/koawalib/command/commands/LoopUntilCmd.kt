package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.subsystem.KSubsystem

@Suppress("unused")
open class LoopUntilCmd(
    private val action: () -> Unit,
    private val endCondition: () -> Boolean,
    vararg requirements: KSubsystem
) : Cmd() {
    final override fun execute() {
        action.invoke()
    }

    final override val isFinished: Boolean
        get() = endCondition.invoke()

    init {
        addRequirements(*requirements)
    }
}
