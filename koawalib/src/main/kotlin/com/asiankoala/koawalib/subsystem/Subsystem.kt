package com.asiankoala.koawalib.subsystem

import com.asiankoala.koawalib.command.KScheduler
import com.asiankoala.koawalib.command.commands.Cmd
import com.asiankoala.koawalib.util.Periodic

interface Subsystem : Periodic {

    override fun periodic() {
    }

    fun setDefaultCommand(cmd: Cmd) {
        KScheduler.setDefaultCommand(this, cmd)
    }

    fun getDefaultCommand(): Cmd {
        return KScheduler.getDefaultCommand(this)
    }

    fun getCurrentCommand(): Cmd? {
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
