package com.asiankoala.koawalib.command.group

import com.asiankoala.koawalib.command.commands.Command
import java.util.*

/**
 * Run multiple commands in parallel until one of them finish
 * @param commands Commands to run in parallel
 */
class ParallelRaceGroup(vararg commands: Command) : CommandGroupBase() {
    private val commands: MutableSet<Command> = HashSet()
    private var mRunsWhenDisabled = false
    private var finished = true

    override fun addCommands(vararg commands: Command) {
        assertUngrouped(*commands)

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
            mRunsWhenDisabled = mRunsWhenDisabled
        }
    }

    override fun initialize() {
        finished = false
        commands.forEach(Command::initialize)
    }

    override fun execute() {
        commands.forEach {
            it.execute()
            if (it.isFinished) {
                finished = true
                it.end()
            }
        }
    }

    override fun end() {
        commands.forEach {
            if (!it.isFinished) {
                it.end()
            }
        }
    }

    override val isFinished: Boolean get() = finished

    init {
        addCommands(*commands)
    }
}
