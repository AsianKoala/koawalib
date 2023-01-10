package com.asiankoala.koawalib.command.commands

import com.qualcomm.robotcore.util.ElapsedTime

/**
 * Dummy command, finishes after n seconds
 * @param seconds amount of seconds to wait until finish
 */
class WaitCmd(private val seconds: Double) : Cmd() {
    private val timer = ElapsedTime()

    override fun initialize() {
        timer.reset()
    }

    override fun execute() {}

    override fun end() {
        timer.reset()
    }

    override val isFinished: Boolean get() = timer.seconds() > seconds
}
