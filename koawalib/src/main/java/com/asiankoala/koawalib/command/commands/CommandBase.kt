package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.subsystem.Subsystem

abstract class CommandBase : Command {
    protected val mRequirements: MutableSet<Subsystem> = HashSet()

    fun addRequirements(vararg subsystems: Subsystem) {
        mRequirements.addAll(subsystems)
    }

    override fun getRequirements(): Set<Subsystem> {
        return mRequirements
    }

    fun name(commandName: String): Command {
        _name = commandName
        return this
    }

    private var _name: String? = null
    override val name: String get() = _name ?: this.javaClass.simpleName
}
