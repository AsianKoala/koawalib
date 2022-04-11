package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.command.CommandScheduler
import com.asiankoala.koawalib.util.OpModeState

// schedule a watchdog to cont. schedule other commands
class Watchdog(
    private val condition: () -> Boolean,
    private val toSchedule: Command
) : CommandBase() {

    override fun execute() {
        if (condition.invoke() && CommandScheduler.opModeInstance.opmodeState == OpModeState.LOOP) {
            toSchedule.schedule()
        }
    }

    override val isFinished: Boolean = false
}
