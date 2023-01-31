package com.asiankoala.koawalib.command.commands

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
