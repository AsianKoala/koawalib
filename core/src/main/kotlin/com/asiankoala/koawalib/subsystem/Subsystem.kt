package com.asiankoala.koawalib.subsystem

import com.asiankoala.koawalib.command.KScheduler
import com.asiankoala.koawalib.command.commands.Cmd
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.util.Periodic

abstract class Subsystem : Periodic {
    val name: String get() = this.javaClass.simpleName
    /**
     * Set the default command of a subsystem. Default commands run when no other command requires the specified subsystem
     * Note: default commands must not end
     */
    var defaultCommand: Cmd? = null
        set(value) {
            require(value != null && value.requirements.size == 1 && this in value.requirements)
            Logger.logInfo("set default command of $name to $value")
            field = value
        }

    fun register() {
        KScheduler.registerSubsystem(this)
    }

    fun unregister() {
        KScheduler.unregisterSubsystem(this)
    }

    override fun periodic() {}

    init {
        register()
    }
}
