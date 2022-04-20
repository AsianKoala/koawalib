package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.subsystem.Subsystem

/**
 * Provided implementation of Command. To create custom commands, extend this class
 * @see Command
 */
abstract class CommandBase : Command {
    internal val mRequirements: MutableSet<Subsystem> = HashSet()
    private var _name: String? = null
    override val name: String get() = _name ?: this.javaClass.simpleName

    /**
     * @param subsystems subsystem requirements of the command
     */
    protected fun addRequirements(vararg subsystems: Subsystem) {
        mRequirements.addAll(subsystems)
    }

    override fun getRequirements(): Set<Subsystem> {
        return mRequirements
    }

    /**
     * Name the current command, which shows up in the logger.
     */
    fun withName(commandName: String): Command {
        _name = commandName
        return this
    }

    override fun toString(): String {
        return name
    }
}
