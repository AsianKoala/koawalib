package com.asiankoala.koawalib.command.group

import com.asiankoala.koawalib.command.commands.Command
import java.util.*

class ParallelDeadlineGroup(private var mDeadline: Command, vararg commands: Command) : CommandGroupBase() {
    // maps commands in this group to whether they are still running
    private val mCommands: MutableMap<Command, Boolean> = HashMap()
    private var mRunWhenDisabled = true

    fun setDeadline(deadline: Command) {
        if (!mCommands.containsKey(deadline)) {
            addCommands(deadline)
        }
        mDeadline = deadline
    }

    override fun addCommands(vararg commands: Command) {
        requireUngrouped(*commands)

        check(!mCommands.containsValue(true)) { "Commands cannot be added to a CommandGroup while the group is running" }

        registerGroupedCommands(*commands)

        for (command in commands) {
            require(Collections.disjoint(command.getRequirements(), mRequirements)) {
                (
                    "Multiple commands in a parallel group cannot" +
                        "require the same subsystems"
                    )
            }
            mCommands[command] = false
            mRequirements.addAll(command.getRequirements())
            mRunWhenDisabled = mRunWhenDisabled
        }
    }

    override fun initialize() {
        for (commandRunning in mCommands.entries) {
            commandRunning.key.initialize()
            commandRunning.setValue(true)
        }
    }

    override fun execute() {
        for (commandRunning in mCommands.entries) {
            if (!commandRunning.value) {
                continue
            }
            commandRunning.key.execute()
            if (commandRunning.key.isFinished) {
                commandRunning.key.end(false)
                commandRunning.setValue(false)
            }
        }
    }

    override fun end(interrupted: Boolean) {
        for ((key, value) in mCommands) {
            if (value) {
                key.end(true)
            }
        }
    }

    override val isFinished: Boolean
        get() = mDeadline.isFinished

    init {
        addCommands(*commands)
        if (!mCommands.containsKey(mDeadline)) {
            addCommands(mDeadline)
        }
    }
}
