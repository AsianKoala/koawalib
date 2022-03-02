package com.asiankoala.koawalib.command.group

import com.asiankoala.koawalib.command.commands.Command
import java.util.*
import kotlin.collections.HashSet

class ParallelRaceGroup(vararg commands: Command) : CommandGroupBase() {
    private val commands: MutableSet<Command> = HashSet()
    private var mRunsWhenDisabled = false
    private var finished = true

    override fun addCommands(vararg commands: Command) {
        requireUngrouped(*commands)

        if (!finished) {
            throw IllegalStateException("Cannot add commands to group while group is running")
        }

        registerGroupedCommands(*commands)

        commands.forEach {
            if (!Collections.disjoint(it.getRequirements(), mRequirements)) {
                throw IllegalArgumentException("Multiple commands cannot require the same subsystem")
            }

            this.commands.add(it)
            mRequirements.addAll(it.getRequirements())
            mRunsWhenDisabled = mRunsWhenDisabled && it.runsWhenDisabled
        }
    }

    override fun init() {
        finished = false
        commands.forEach(Command::init)
    }

    override fun execute() {
        commands.forEach {
            it.execute()
            if (it.isFinished) {
                finished = true
                it.end(false)
            }
        }
    }

    override fun end(interrupted: Boolean) {
        commands.forEach {
            if (!it.isFinished) {
                it.end(true)
            }
        }
    }

    override val isFinished: Boolean get() = finished

    override val runsWhenDisabled: Boolean get() = mRunsWhenDisabled

    init {
        addCommands(*commands)
    }
}
