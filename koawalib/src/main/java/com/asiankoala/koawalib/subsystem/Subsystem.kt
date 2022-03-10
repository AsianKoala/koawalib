package com.asiankoala.koawalib.subsystem

import com.asiankoala.koawalib.command.CommandScheduler
import com.asiankoala.koawalib.command.commands.Command
import com.asiankoala.koawalib.util.Periodic

interface Subsystem : Periodic {

    override fun periodic() {

    }

    fun setDefaultCommand(command: Command) {
        CommandScheduler.setDefaultCommand(this, command)
    }

    fun getDefaultCommand(): Command {
        return CommandScheduler.getDefaultCommand(this)
    }

    fun getCurrentCommand(): Command {
        return CommandScheduler.requiring(this)
    }

    fun register() {
        CommandScheduler.registerSubsystem(this)
    }

    val name: String get() = this.javaClass.simpleName
}
