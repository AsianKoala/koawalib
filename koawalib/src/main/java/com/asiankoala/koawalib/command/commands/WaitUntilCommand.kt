package com.asiankoala.koawalib.command.commands

// used in command groups
class WaitUntilCommand(private val condition: () -> Boolean) : CommandBase() {
    override fun execute() {
    }

    override val runsWhenDisabled: Boolean = true
    override val isFinished: Boolean get() = condition.invoke()
}
