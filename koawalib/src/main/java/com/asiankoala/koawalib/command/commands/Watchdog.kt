package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.command.CommandScheduler

// schedule a watchdog to cont. schedule other commands
class Watchdog(
    private val condition: () -> Boolean,
    private val toSchedule: Command
) : CommandBase() {

    override fun execute() {
        if (condition.invoke() && CommandScheduler.isOpModeLooping) {
            toSchedule.schedule()
        }
    }

    override val isFinished: Boolean = false
    override val runsWhenDisabled: Boolean = true
}
