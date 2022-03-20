package com.asiankoala.koawalib.command

import com.asiankoala.koawalib.command.commands.*
import com.asiankoala.koawalib.command.group.CommandGroupBase
import com.asiankoala.koawalib.subsystem.Subsystem
import java.util.Collections
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

@Suppress("unused")
object CommandScheduler {
    private val scheduledCommands: MutableList<Command> = ArrayList()
    private val scheduledCommandRequirements: MutableMap<Subsystem, Command> = LinkedHashMap()
    private val subsystems: MutableMap<Subsystem, Command?> = LinkedHashMap()

    private val toSchedule: MutableList<Command> = ArrayDeque()
    private val toCancel: MutableList<Command> = ArrayDeque()

    private val allMaps = listOf(scheduledCommandRequirements, subsystems)
    private val allLists = listOf(scheduledCommands, toCancel, toSchedule)

    internal var isOpModeLooping = false
        private set

    private fun initCommand(command: Command, cRequirements: Set<Subsystem>) {
        command.init()
        scheduledCommands.add(command)
        CommandOpMode.logger.logInfo("command ${command.name} initialized")
        cRequirements.forEach { scheduledCommandRequirements[it] = command }
    }

    private fun Command.scheduleThis() {
        if (CommandGroupBase.getGroupedCommands().contains(this)) {
            CommandOpMode.logger.logError("command ${this.name}: Command in command groups cannot be independently scheduled")
        } else {
            CommandOpMode.logger.logDebug("command ${this.name}: Command not in any command groups")
        }

        val requirements = this.getRequirements()

        if (Collections.disjoint(scheduledCommandRequirements.keys, requirements)) {
            initCommand(this, requirements)
            CommandOpMode.logger.logDebug("command ${this.name}: Command disjoint with scheduledRequirementKeys")
        } else {
            requirements.forEach {
                if (scheduledCommandRequirements.containsKey(it)) {
                    val scheduled = scheduledCommandRequirements[it]!!
                    scheduled.cancel()
                    CommandOpMode.logger.logWarning("command ${this.name}: Command caused command ${scheduled.name} to cancel")
                }
            }

            initCommand(this, requirements)
            CommandOpMode.logger.logDebug("command ${this.name}: Command initialized following cancellation of commands with overlapping requirements")
        }
    }

    private fun Command.cancelThis() {
        if(!scheduledCommands.contains(this)) {
            return
        }

        this.end(true)
        CommandOpMode.logger.logInfo("command ${this.name} canceled")
        scheduledCommands.remove(this)
        scheduledCommandRequirements.keys.removeAll(this.getRequirements())
    }

    internal fun run() {
        CommandOpMode.logger.logDebug("CommandScheduler entered run()")
        CommandOpMode.logger.logDebug("amount of scheduled commands before run(): ${scheduledCommands.size + toSchedule.size}")

        toSchedule.forEach { it.scheduleThis() }
        toCancel.forEach { it.cancelThis() }

        toSchedule.clear()
        toCancel.clear()

        subsystems.keys.forEach(Subsystem::periodic)

        val iterator = scheduledCommands.iterator()
        while (iterator.hasNext()) {
            val command = iterator.next()

            command.execute()
            CommandOpMode.logger.logInfo("command ${command.name} executed")

            if (command.isFinished) {
                command.end(false)
                CommandOpMode.logger.logInfo("command ${command.name} finished")
                iterator.remove()
                scheduledCommandRequirements.keys.removeAll(command.getRequirements())
            }
        }

        subsystems.forEach { (k, v) ->
            if (!scheduledCommandRequirements.containsKey(k) && v != null) {
                schedule(v)
            }
        }

        CommandOpMode.logger.logDebug("amount of scheduled commands after run(): ${scheduledCommands.size}")
        CommandOpMode.logger.logDebug("CommandScheduler exited run()")
    }

    fun schedule(vararg commands: Command) {
        commands.forEach {
            toSchedule.add(it)
            CommandOpMode.logger.logDebug("added ${it.name} to toSchedule array")
        }
    }

    fun cancel(vararg commands: Command) {
        toCancel.addAll(commands)
    }

    internal fun resetScheduler() {
        allMaps.forEach(MutableMap<*, *>::clear)
        allLists.forEach(MutableList<*>::clear)
        isOpModeLooping = false
    }

    fun startOpModeLooping() {
        isOpModeLooping = true
    }

    fun registerSubsystem(vararg requestedSubsystems: Subsystem) {
        requestedSubsystems.forEach { this.subsystems[it] = null }
    }

    fun unregisterSubsystem(vararg requestedSubsystems: Subsystem) {
        requestedSubsystems.forEach { CommandOpMode.logger.logInfo("unregistered subsystem ${it.name}") }
        this.subsystems.keys.removeAll(requestedSubsystems)
    }

    fun setDefaultCommand(subsystem: Subsystem, command: Command) {
        if (!command.getRequirements().contains(subsystem)) {
            CommandOpMode.logger.logError("command ${command.name}: default commands must require subsystem")
        }

        if (command.isFinished) {
            CommandOpMode.logger.logError("command ${command.name}: default commands must not end")
        }

        CommandOpMode.logger.logInfo("set default command of ${subsystem.name} to ${command.name}")
        subsystems[subsystem] = command
    }

    fun getDefaultCommand(subsystem: Subsystem): Command {
        return subsystems[subsystem]!!
    }

    fun cancelAll() {
        scheduledCommands.forEach(Command::cancel)
        CommandOpMode.logger.logInfo("canceled all commands")
    }

    fun isScheduled(vararg commands: Command): Boolean {
        return scheduledCommands.containsAll(commands.toList())
    }

    fun requiring(subsystem: Subsystem): Command {
        return scheduledCommandRequirements[subsystem]!!
    }

    /**
     * internal because command scheduler uses this
     */
    internal fun addPeriodic(action: () -> Unit) {
        InfiniteCommand(action).schedule()
        CommandOpMode.logger.logInfo("added periodic")
    }

    fun scheduleWatchdog(condition: () -> Boolean, command: Command) {
        schedule(Watchdog(condition, command))
        CommandOpMode.logger.logInfo("added watchdog ${command.name}")
    }
}
