package com.asiankoala.koawalib.command.group

import com.asiankoala.koawalib.command.commands.Cmd
import com.asiankoala.koawalib.logger.Logger

/**
 * Run multiple commands sequentially, finishing when the last one finishes
 * @param cmds Commands to run sequentially
 */
open class SequentialGroup(vararg cmds: Cmd) : Cmd(), Group {
    private val cmds: MutableList<Cmd> = ArrayList()
    private var idx = -1

    final override fun addCommands(vararg cmds: Cmd) {
        assert(idx == -1) { "Commands cannot be added to a CommandGroup while the group is running" }
        this.cmds.addAll(cmds)
    }

    override val currentCmdNames: List<String>
        get() {
            if(idx !in cmds.indices) return listOf("none")
            return listOf(cmds[idx].name)
        }

    override fun initialize() {
        idx = 0
        cmds[0].initialize()
    }

    override fun execute() {
        var cmd = cmds[idx]
        cmd.execute()
        Logger.logDebug("command ${cmd.name} of group $name executed")
        if (cmd.isFinished) {
            cmd.end()
            idx++
            if (idx < cmds.size) {
                cmd = cmds[idx]
                cmd.initialize()
                requirements.clear()
                requirements.addAll(cmd.requirements)
            }
        }
    }

    override fun end() {
        if (idx in cmds.indices) cmds[idx].end()
        idx = -1
    }

    override val isFinished: Boolean
        get() = idx !in cmds.indices

    init {
        addCommands(*cmds)

        if (this.cmds.isEmpty()) Logger.logWarning("sequential group $name is empty")
    }
}
