package com.asiankoala.koawalib.subsystem

import com.asiankoala.koawalib.command.KScheduler
import com.asiankoala.koawalib.command.commands.Cmd
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.util.Periodic

abstract class Subsystem : Periodic {
    /**
     * Set the default command of a subsystem. Default commands run when no other command requires the specified subsystem
     * Note: default commands must not end
     */
    internal var defaultCommand: Cmd? = null
        set(value) {
            if(value == null) return
            if (value.requirements.size != 1 || this !in value.requirements) {
                throw Exception("command ${name}: default commands must require only subsystem $name")
            }
            Logger.logInfo("set default command of $name to $value")
            field = value
        }

    override fun periodic() {}

    fun register() {
        KScheduler.registerSubsystem(this)
    }

    fun unregister() {
        KScheduler.unregisterSubsystem(this)
    }

    val name: String get() = this.javaClass.simpleName

    init {
        register()
    }
}
