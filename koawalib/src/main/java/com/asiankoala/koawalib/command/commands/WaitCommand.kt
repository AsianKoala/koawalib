package com.asiankoala.koawalib.command.commands

import com.qualcomm.robotcore.util.ElapsedTime

// timer command
open class WaitCommand(private val seconds: Double) : CommandBase() {
    private val timer = ElapsedTime()

    override fun init() {
        timer.reset()
    }

    override fun execute() {
    }

    override fun end(interrupted: Boolean) {
        timer.reset()
    }

    override val isFinished: Boolean get() = timer.seconds() > seconds
}
