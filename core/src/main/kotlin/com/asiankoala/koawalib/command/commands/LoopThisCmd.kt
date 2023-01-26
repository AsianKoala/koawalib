package com.asiankoala.koawalib.command.commands

open class LoopThisCmd(
    private val cmd: Cmd,
    private val shouldEnd: () -> Boolean
) : Cmd() {
    override fun initialize() {
        + cmd
    }

    override fun execute() {
        if(cmd.isFinished) {
            + cmd
        }
    }

    override val isFinished: Boolean
        get() = shouldEnd.invoke()
}