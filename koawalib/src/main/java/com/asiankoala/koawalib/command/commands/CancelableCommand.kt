package com.asiankoala.koawalib.command.commands

/**
 * A command that cancels upon fulfillment of a certain condition
 *
 * @param condition the condition required to cancel the command
 * @param command the command that is ran until either the command finishes naturally or the condition reaches fulfillment
 */
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

    override fun end() {
        command.end()
    }

    override val isFinished: Boolean
        get() = command.isFinished
}
