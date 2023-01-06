package com.asiankoala.koawalib.command.commands

/**
 * Dummy command, finishes when condition is called
 * @param condition finish condition
 */
open class WaitUntilCmd(private val condition: () -> Boolean) : Cmd() {
    override fun execute() {}
    override val isFinished: Boolean get() = condition.invoke()
}
