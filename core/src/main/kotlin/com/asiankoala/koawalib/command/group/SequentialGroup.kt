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
        require(idx == -1)
        this.cmds.addAll(cmds)
    }

    override fun initialize() {
        idx = 0
        cmds[0].initialize()
    }

    override fun execute() {
        var cmd = cmds[idx]
        cmd.execute()
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
        if (cmds.isEmpty()) Logger.logWarning("sequential group $name is empty")
    }
}
