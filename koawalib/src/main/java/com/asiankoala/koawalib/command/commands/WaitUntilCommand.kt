package com.asiankoala.koawalib.command.commands

// used in command groups
open class WaitUntilCommand(private val condition: () -> Boolean) : CommandBase() {
    override fun execute() {
    }

    override val isFinished: Boolean get() = condition.invoke()
}
