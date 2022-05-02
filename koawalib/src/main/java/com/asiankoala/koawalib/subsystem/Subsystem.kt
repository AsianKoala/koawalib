package com.asiankoala.koawalib.subsystem

import com.asiankoala.koawalib.command.KScheduler
import com.asiankoala.koawalib.command.commands.Command
import com.asiankoala.koawalib.util.Periodic

interface Subsystem : Periodic {

    override fun periodic() {
    }

    fun setDefaultCommand(command: Command) {
        KScheduler.setDefaultCommand(this, command)
    }

    fun getDefaultCommand(): Command {
        return KScheduler.getDefaultCommand(this)
    }

    fun getCurrentCommand(): Command? {
        return KScheduler.requiring(this)
    }

    fun register() {
        KScheduler.registerSubsystem(this)
    }

    fun unregister() {
        KScheduler.unregisterSubsystem(this)
    }

    val name: String get() = this.javaClass.simpleName
}
