package com.asiankoala.koawalib.command.group

import com.asiankoala.koawalib.command.commands.Cmd
import com.asiankoala.koawalib.util.disjoint
import java.util.*
import kotlin.collections.HashMap

/**
 * CommandGroups are used to run multiple commands. To create a custom CommandGroup, extend this class
 */
open class ParallelGroup(private val endCond: (Map<Cmd, Boolean>) -> Boolean = { !it.containsValue(true) }, vararg cmds: Cmd) : Cmd(), Group {
    constructor(vararg commands: Cmd) : this(cmds = commands)

    private val cmdMap = HashMap<Cmd, Boolean>()

    final override fun addCommands(vararg cmds: Cmd) {
        if (cmdMap.containsValue(true)) {
            throw IllegalStateException("Commands cannot be added to a CommandGroup while the group is running")
        }

        cmds.forEach {
            if (!(it.requirements disjoint requirements)) {
                throw IllegalStateException("Multiple commands in a parallel group cannot require the same subsystems")
            }

            cmdMap[it] = false
            requirements.addAll(it.requirements)
        }
    }

    override val currentCmdNames: List<String>
        get() = cmdMap.keys.map { it.name }

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
        addCommands(*cmds)
    }
}
