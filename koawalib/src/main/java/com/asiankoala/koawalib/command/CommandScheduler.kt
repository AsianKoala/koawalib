package com.asiankoala.koawalib.command

import com.asiankoala.koawalib.command.commands.Command
import com.asiankoala.koawalib.command.commands.InfiniteCommand
import com.asiankoala.koawalib.command.commands.Watchdog
import com.asiankoala.koawalib.command.group.CommandGroupBase
import com.asiankoala.koawalib.subsystem.Subsystem
import java.util.Collections
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

object CommandScheduler {
    private val scheduledCommands: MutableList<Command> = ArrayList()
    private val scheduledCommandRequirements: MutableMap<Subsystem, Command> = LinkedHashMap()
    private val subsystems: MutableMap<Subsystem, Command?> = LinkedHashMap()

    private val initActions: MutableList<(Command) -> Unit> = ArrayList()
    private val executeActions: MutableList<(Command) -> Unit> = ArrayList()
    private val cancelActions: MutableList<(Command) -> Unit> = ArrayList()
    private val finishActions: MutableList<(Command) -> Unit> = ArrayList()

    private val toSchedule: MutableList<Command> = ArrayList()
    private val toCancel: MutableList<Command> = ArrayList()

    private val allMaps = listOf(scheduledCommandRequirements, subsystems)
    private val allLists = listOf(
        initActions, executeActions,
        cancelActions, finishActions, toCancel, toSchedule
    )

    private var mInRunLoop = false

    private lateinit var mOpMode: CommandOpMode

    val isOpModeLooping get() = mOpMode.isLooping

    private fun initCommand(command: Command, cRequirements: Set<Subsystem>) {
        command.init()
        scheduledCommands.add(command)
        initActions.forEach { it.invoke(command) }
        cRequirements.forEach { scheduledCommandRequirements[it] = command }
    }

    private fun schedule(command: Command) {
        if (mInRunLoop) {
            toSchedule
            return
        }

        if (CommandGroupBase.getGroupedCommands().contains(command)) {
            throw IllegalArgumentException("A command that is part of a command group cannot be independently scheduled")
        }

        val requirements = command.getRequirements()

        if (Collections.disjoint(scheduledCommandRequirements.keys, requirements)) {
            initCommand(command, requirements)
        } else {
            requirements.forEach {
                if (scheduledCommandRequirements.containsKey(it)) {
                    scheduledCommandRequirements[it]!!.cancel()
                }
            }

            initCommand(command, requirements)
        }
    }

    fun resetScheduler() {
        allMaps.forEach(MutableMap<*, *>::clear)
        allLists.forEach(MutableList<*>::clear)
        mInRunLoop = false
    }

    fun printTelemetry() {
        println()
        println()
        println("scheduled command size ${scheduledCommands.size}")
        println("requirements size ${scheduledCommandRequirements.size}")
        println("subsystems size ${subsystems.size}")
        println("to schedule size ${toSchedule.size} ")
        println("to cancel size ${toCancel.size}")
    }

    fun setOpMode(opMode: CommandOpMode) {
        mOpMode = opMode
    }

    fun schedule(interruptible: Boolean, vararg commands: Command) {
        commands.forEach { schedule(interruptible, it) }
    }

    fun schedule(vararg commands: Command) {
        schedule(true, *commands)
    }

    fun run() {
        subsystems.keys.forEach(Subsystem::periodic)

        mInRunLoop = true
        val iterator = scheduledCommands.iterator()
        while (iterator.hasNext()) {
            val command = iterator.next()

            command.execute()
            executeActions.forEach { it.invoke(command) }

            if (command.isFinished) {
                command.end(false)
                finishActions.forEach { it.invoke(command) }
                iterator.remove()
                scheduledCommandRequirements.keys.removeAll(command.getRequirements())
            }
        }

        mInRunLoop = false

        toSchedule.forEach(Command::schedule)
        toCancel.forEach(Command::cancel)

        toSchedule.clear()
        toCancel.clear()

        subsystems.forEach { (k, v) ->
            if (!scheduledCommandRequirements.containsKey(k) && v != null) {
                schedule(v)
            }
        }
    }

    fun registerSubsystem(vararg subsystems: Subsystem) {
        subsystems.forEach { this.subsystems[it] = null }
    }

    fun unregisterSubsystem(vararg subsystems: Subsystem) {
        this.subsystems.keys.removeAll(subsystems)
    }

    fun setDefaultCommand(subsystem: Subsystem, command: Command) {
        if (!command.getRequirements().contains(subsystem)) {
            throw IllegalArgumentException("Default commands must require subsystem")
        }

        if (command.isFinished) {
            throw java.lang.IllegalArgumentException("Default commands should not end")
        }

        subsystems[subsystem] = command
    }

    fun getDefaultCommand(subsystem: Subsystem): Command {
        return subsystems[subsystem]!!
    }

    fun cancel(vararg commands: Command) {
        if (mInRunLoop) {
            toCancel.addAll(commands)
            return
        }

        commands.forEach {
            if (!scheduledCommands.contains(it)) {
                return@forEach
            }

            it.end(true)
            cancelActions.forEach { action -> action.invoke(it) }
            scheduledCommands.remove(it)
            scheduledCommandRequirements.keys.removeAll(it.getRequirements())
        }
    }

    fun cancelAll() {
        scheduledCommands.forEach(Command::cancel)
    }

    fun isScheduled(vararg commands: Command): Boolean {
        return scheduledCommands.containsAll(commands.toList())
    }

    fun requiring(subsystem: Subsystem): Command {
        return scheduledCommandRequirements[subsystem]!!
    }

    fun addPeriodic(action: () -> Unit) {
        InfiniteCommand(action).schedule()
    }

    fun scheduleWatchdog(condition: () -> Boolean, command: Command) {
        schedule(Watchdog(condition, command))
    }
}
