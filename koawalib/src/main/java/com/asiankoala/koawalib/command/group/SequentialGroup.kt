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
    private var idxOnEnd = -1
    private var hasInitialized = false
    private var hasEnded = false

    final override fun addCommands(vararg commands: Command) {
        assert(idx == -1) { "Commands cannot be added to a CommandGroup while the group is running" }
        cmds.addAll(commands)
    }

    override fun initialize() {
        idx = 0
        cmds[0].initialize()
        hasInitialized = true
    }

    override fun execute() {
        if (!hasInitialized) {
            throw Exception("sequential group $name has not initialized yet")
        } else if (hasEnded) {
            throw Exception("sequential group $name has ended on idx $idxOnEnd")
        }

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
        idxOnEnd = idx
        idx = -1
        hasEnded = true
    }

    override val isFinished: Boolean
        get() = idx !in cmds.indices

    init {
        addCommands(*commands)

        if (cmds.isEmpty()) Logger.logWarning("sequential group $name is empty")
    }
}
