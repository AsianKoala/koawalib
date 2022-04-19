package com.asiankoala.koawalib.command.group

import com.asiankoala.koawalib.command.commands.Command
import com.asiankoala.koawalib.logger.Logger

open class SequentialCommandGroup(vararg commands: Command) : CommandGroupBase() {
    private val mCommands: MutableList<Command> = ArrayList()
    private var mCurrentCommandIndex = -1

    private fun initCurrentCommand() {
        val currentCommand = mCommands[mCurrentCommandIndex]
        currentCommand.initialize()
        mRequirements.clear()
        mRequirements.addAll(currentCommand.getRequirements())
    }

    final override fun addCommands(vararg commands: Command) {
        requireUngrouped(*commands)
        assert(mCurrentCommandIndex == -1) { "Commands cannot be added to a CommandGroup while the group is running" }
        registerGroupedCommands(*commands)
        for (command in commands) {
            mCommands.add(command)
//            mRequirements.addAll(command.getRequirements())
        }
    }

    override fun initialize() {
        mCurrentCommandIndex = 0
        if (mCommands.isNotEmpty()) {
            mCommands[0].initialize()
        }
    }

    override fun execute() {
        if (mCommands.isEmpty()) {
            Logger.logWarning("command group base $name ended prematurely")
            return
        }

        if(mCurrentCommandIndex !in mCommands.indices) {
            Logger.logWarning("command group base $name ended prematurely")
            return
        }

        val currentCommand = mCommands[mCurrentCommandIndex]
        currentCommand.execute()
        Logger.logInfo("command ${currentCommand.name} of group $name executed")
        if (currentCommand.isFinished) {
            currentCommand.end()
            mCurrentCommandIndex++
            if (mCurrentCommandIndex < mCommands.size) {
                initCurrentCommand()
            }
        }
    }

    override fun end() {
        if (mCurrentCommandIndex < mCommands.size) {
            mCommands[mCurrentCommandIndex].end()
        }
        mCurrentCommandIndex = -1
    }

    override val isFinished: Boolean
        get() = mCurrentCommandIndex !in mCommands.indices

    init {
        addCommands(*commands)
    }
}
