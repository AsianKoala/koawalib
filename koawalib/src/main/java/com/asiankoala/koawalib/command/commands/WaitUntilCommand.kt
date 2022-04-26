package com.asiankoala.koawalib.command.commands

/**
 * Dummy command, finishes when condition is called
 * @param condition finish condition
 */
open class WaitUntilCommand(private val condition: () -> Boolean) : Command() {
    override fun execute() {
    }

    override val isFinished: Boolean get() = condition.invoke()
}
