package com.asiankoala.koawalib.command.group

import com.asiankoala.koawalib.command.commands.Cmd
import com.asiankoala.koawalib.util.internal.disjoint

open class ParallelGroup(
    private val endCond: (Map<Cmd, Boolean>) -> Boolean = { !it.containsValue(true) },
    vararg cmds: Cmd
) : Cmd(), Group {
    constructor(vararg commands: Cmd) : this(cmds = commands)
    private val cmdMap = HashMap<Cmd, Boolean>()

    final override fun addCommands(vararg cmds: Cmd) {
        cmds.forEach {
            require(it.requirements disjoint requirements)
            cmdMap[it] = false
            requirements.addAll(it.requirements)
        }
    }

    override fun initialize() {
        cmdMap.entries.forEach {
            it.key.initialize()
            it.setValue(true)
        }
    }

    override fun execute() {
        cmdMap.entries.forEach {
            if (!it.value) return@forEach
            val cmd = it.key
            cmd.execute()
            if (cmd.isFinished) {
                cmd.end()
                it.setValue(false)
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
