package com.asiankoala.koawalib.command.group

import com.asiankoala.koawalib.command.commands.Command
import com.asiankoala.koawalib.logger.Logger

/**
 * Run multiple commands sequentially, finishing when the last one finishes
 * @param commands Commands to run sequentially
 */
open class SequentialGroup(vararg commands: Command) : Command(), Group {
    private val cmds: MutableList<Command> = ArrayList()
    private var idx = -1

    final override fun addCommands(vararg commands: Command) {
        assert(idx == -1) { "Commands cannot be added to a CommandGroup while the group is running" }
        cmds.addAll(commands)
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
        addCommands(*commands)

        if (cmds.isEmpty()) Logger.logWarning("sequential group $name is empty")
    }
}
