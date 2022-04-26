package com.asiankoala.koawalib.command.commands

/**
 * Command that chooses between two different commands based on condition
 * @param onTrue    the command to run if the condition is true
 * @param onFalse   the command to run if the condition is false
 * @param condition the condition to determine which command to run
 */
@Suppress("unused")
class ChooseCmd(private val onTrue: Cmd, private val onFalse: Cmd, private val condition: () -> Boolean) : Cmd() {
    private val selected by lazy { if(condition.invoke()) onTrue else onFalse }

    override fun initialize() {
        requirements.addAll(selected.requirements)
        selected.initialize()
    }

    override fun execute() {
        selected.execute()
    }

    override fun end() {
        selected.end()
    }

    override val isFinished: Boolean
        get() = selected.isFinished
}
