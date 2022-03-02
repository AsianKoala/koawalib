package com.asiankoala.koawalib.command.group

import com.asiankoala.koawalib.command.commands.Command

open class SequentialCommandGroup(vararg commands: Command) : CommandGroupBase() {
    private val mCommands: MutableList<Command> = ArrayList()
    private var mCurrentCommandIndex = -1
    private var mRunWhenDisabled = true

    final override fun addCommands(vararg commands: Command) {
        requireUngrouped(*commands)
        check(mCurrentCommandIndex == -1) { "Commands cannot be added to a CommandGroup while the group is running" }
        registerGroupedCommands(*commands)
        for (command in commands) {
            mCommands.add(command)
            mRequirements.addAll(command.getRequirements())
            mRunWhenDisabled = mRunWhenDisabled and command.runsWhenDisabled
        }
    }

    override fun init() {
        mCurrentCommandIndex = 0
        if (mCommands.isNotEmpty()) {
            mCommands[0].init()
        }
    }

    override fun execute() {
        if (mCommands.isEmpty()) {
            return
        }
        val currentCommand = mCommands[mCurrentCommandIndex]
        currentCommand.execute()
        if (currentCommand.isFinished) {
            currentCommand.end(false)
            mCurrentCommandIndex++
            if (mCurrentCommandIndex < mCommands.size) {
                mCommands[mCurrentCommandIndex].init()
            }
        }
    }

    override fun end(interrupted: Boolean) {
        if (interrupted && mCommands.isNotEmpty()) {
            mCommands[mCurrentCommandIndex].end(true)
        }
        mCurrentCommandIndex = -1
    }

    override val isFinished: Boolean
        get() = mCurrentCommandIndex == mCommands.size

    override val runsWhenDisabled: Boolean
        get() = mRunWhenDisabled

    init {
        addCommands(*commands)
    }
}
