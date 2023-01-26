package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.command.KScheduler
import com.asiankoala.koawalib.util.OpModeState

class WatchdogCmd(
    private val toSchedule: Cmd,
    private val condition: () -> Boolean,
) : Cmd() {
    override fun execute() {
        if (condition.invoke()) {
            toSchedule.schedule()
        }
    }

    override val isFinished: Boolean = false
}
