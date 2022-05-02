package com.asiankoala.koawalib.command.group

import com.asiankoala.koawalib.command.commands.Command
import java.util.*
import kotlin.collections.HashMap

/**
 * CommandGroups are used to run multiple commands. To create a custom CommandGroup, extend this class
 */
open class ParallelGroup(private val endCond: (Map<Command, Boolean>) -> Boolean = { !it.containsValue(true) }, vararg commands: Command) : Command(), Group {
    constructor(vararg cmds: Command) : this(commands = cmds)

    private val cmdMap = HashMap<Command, Boolean>()

    final override fun addCommands(vararg commands: Command) {
        if (cmdMap.containsValue(true)) {
            throw IllegalStateException("Commands cannot be added to a CommandGroup while the group is running")
        }

        commands.forEach {
            if (!Collections.disjoint(it.requirements, requirements)) {
                throw IllegalStateException("Multiple commands in a parallel group cannot require the same subsystems")
            }

            cmdMap[it] = false
            requirements.addAll(it.requirements)
        }
    }

    override fun initialize() {
        for (entry in cmdMap.entries) {
            entry.key.initialize()
            entry.setValue(true)
        }
    }

    override fun execute() {
        for (entry in cmdMap.entries) {
            if (!entry.value) continue
            val cmd = entry.key
            cmd.execute()
            if (cmd.isFinished) {
                cmd.end()
                entry.setValue(false)
            }
        }
    }

    override fun end() {
        cmdMap.forEach { if (it.value) it.key.end() }
    }

    override val isFinished: Boolean
        get() = endCond.invoke(cmdMap)

    init {
        addCommands(*commands)
    }
}
