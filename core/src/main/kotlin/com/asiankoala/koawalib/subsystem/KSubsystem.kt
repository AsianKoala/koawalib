package com.asiankoala.koawalib.subsystem

import com.asiankoala.koawalib.command.KScheduler
import com.asiankoala.koawalib.command.commands.Cmd
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.util.Periodic

/**
 * Subsystems are used to interface hardware with koawalib's command-based model.
 * [KScheduler] uses subsystems to ensure that commands respect requirements
 */
abstract class KSubsystem : Periodic {
    val name: String get() = this.javaClass.simpleName
    /**
     * Set the default command of a subsystem.
     * Default commands run when no other command requires the specified subsystem
     * Must only require this and only this subsystem
     */
    var defaultCommand: Cmd? = null
        set(value) {
            require(value != null && value.requirements.size == 1 && this in value.requirements)
            Logger.logInfo("set default command of $name to $value")
            field = value
        }

    /**
     * Registers the subsystem with KScheduler
     * Shorthand for [KScheduler.registerSubsystem]
     */
    fun register() {
        KScheduler.registerSubsystem(this)
    }

    /**
     * Unregisters the subsystem with KScheduler
     * Shorthand for [KScheduler.unregisterSubsystem]
     */
    fun unregister() {
        KScheduler.unregisterSubsystem(this)
    }

    /**
     * Called periodicly by KScheduler. Contrasts with [defaultCommand]
     */
    override fun periodic() {}

    init {
        register()
    }
}
