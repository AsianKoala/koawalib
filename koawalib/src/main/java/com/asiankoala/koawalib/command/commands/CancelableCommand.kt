package com.asiankoala.koawalib.command.commands

class CancelableCommand(
    private val condition: () -> Boolean,
    private val command: Command
) : CommandBase() {

    override fun initialize() {
        if (condition.invoke()) {
            command.cancel()
        }

        command.initialize()
    }

    override fun execute() {
        if (condition.invoke()) {
            command.cancel()
        }

        command.execute()
    }

    override fun end(interrupted: Boolean) {
        command.end(true)
    }

    override val isFinished: Boolean
        get() = command.isFinished
}
