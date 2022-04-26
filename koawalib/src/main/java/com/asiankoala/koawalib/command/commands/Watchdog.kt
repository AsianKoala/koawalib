package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.command.KScheduler
import com.asiankoala.koawalib.util.OpModeState

internal class Watchdog(
    private val condition: () -> Boolean,
    private val toSchedule: Command
) : Command() {

    override fun execute() {
        if (condition.invoke() && KScheduler.opModeInstance.opmodeState == OpModeState.LOOP) {
            toSchedule.schedule()
        }
    }

    override val isFinished: Boolean = false
}
