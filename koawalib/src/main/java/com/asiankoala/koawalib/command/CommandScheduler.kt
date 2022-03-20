package com.asiankoala.koawalib.command

import com.asiankoala.koawalib.command.commands.*
import com.asiankoala.koawalib.command.group.CommandGroupBase
import com.asiankoala.koawalib.subsystem.Subsystem
import com.asiankoala.koawalib.util.Loggable
import java.util.Collections
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

// TODO: INTERNAL SHIT INSTEAD OF MAKING THEM PUBLIC :RAGE:
@Suppress("unused")
object CommandScheduler : Loggable {
    val scheduledCommands: MutableList<Command> = ArrayList()
    private val scheduledCommandRequirements: MutableMap<Subsystem, Command> = LinkedHashMap()
    private val subsystems: MutableMap<Subsystem, Command?> = LinkedHashMap()

    private val toSchedule: MutableList<Command> = ArrayDeque()
    private val toCancel: MutableList<Command> = ArrayDeque()

    private val allMaps = listOf(scheduledCommandRequirements, subsystems)
    private val allLists = listOf(scheduledCommands, toCancel, toSchedule)

    override var logCount: Int = 0
    override var isLogging = false
    override var isPrinting = true

    var isOpModeLooping = false
        private set

    private fun initCommand(command: Command, cRequirements: Set<Subsystem>) {
        command.init()
        scheduledCommands.add(command)
        logInfo("command ${command.name} initialized")
        cRequirements.forEach { scheduledCommandRequirements[it] = command }
    }

    private fun Command.scheduleThis() {
        if (CommandGroupBase.getGroupedCommands().contains(this)) {
            logError("command ${this.name}: Command in command groups cannot be independently scheduled")
//            throw Exception("Commands in command groups cannot be independently scheduled")
        } else {
            logDebug("command ${this.name}: Command not in any command groups")
        }

        val requirements = this.getRequirements()


        if (Collections.disjoint(scheduledCommandRequirements.keys, requirements)) {
            initCommand(this, requirements)
            logDebug("command ${this.name}: Command disjoint with scheduledRequirementKeys")
        } else {
            requirements.forEach {
                if (scheduledCommandRequirements.containsKey(it)) {
                    val scheduled = scheduledCommandRequirements[it]!!
                    scheduled.cancel()
                    logWarning("command ${this.name}: Command caused command ${scheduled.name} to cancel")
                }
            }

            initCommand(this, requirements)
            logDebug("command ${this.name}: Command initialized following cancellation of commands with overlapping requirements")
        }
    }

    private fun Command.cancelThis() {
        if(!scheduledCommands.contains(this)) {
            return
        }

        this.end(true)
        logInfo("command ${this.name} canceled")
        scheduledCommands.remove(this)
        scheduledCommandRequirements.keys.removeAll(this.getRequirements())
    }

    fun run() {
        logDebug("CommandScheduler entered run()")
        logDebug("amount of scheduled commands before run(): ${scheduledCommands.size + toSchedule.size}")

        toSchedule.forEach { it.scheduleThis() }
        toCancel.forEach { it.cancelThis() }

        toSchedule.clear()
        toCancel.clear()

        subsystems.keys.forEach(Subsystem::periodic)

        val iterator = scheduledCommands.iterator()
        while (iterator.hasNext()) {
            val command = iterator.next()

            command.execute()
            logInfo("command ${command.name} executed")

            if (command.isFinished) {
                command.end(false)
                logInfo("command ${command.name} finished")
                iterator.remove()
                scheduledCommandRequirements.keys.removeAll(command.getRequirements())
            }
        }

        subsystems.forEach { (k, v) ->
            if (!scheduledCommandRequirements.containsKey(k) && v != null) {
                schedule(v)
            }
        }

        logDebug("amount of scheduled commands after run(): ${scheduledCommands.size}")
        logDebug("CommandScheduler exited run()")
    }

    fun schedule(vararg commands: Command) {
        commands.forEach {
            toSchedule.add(it)
            logDebug("added ${it.name} to toSchedule array")
        }
    }

    fun cancel(vararg commands: Command) {
        toCancel.addAll(commands)
    }

    fun resetScheduler() {
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
        requestedSubsystems.forEach { logInfo("unregistered subsystem ${it.name}") }
        this.subsystems.keys.removeAll(requestedSubsystems)
    }

    fun setDefaultCommand(subsystem: Subsystem, command: Command) {
        if (!command.getRequirements().contains(subsystem)) {
            logError("command ${command.name}: default commands must require subsystem")
            throw Exception("Default commands must require subsystem")
        }

        if (command.isFinished) {
            logError("command ${command.name}: default commands must not end")
            throw Exception("Default commands should not end")
        }

        logInfo("set default command of ${subsystem.name} to ${command.name}")
        subsystems[subsystem] = command
    }

    fun getDefaultCommand(subsystem: Subsystem): Command {
        return subsystems[subsystem]!!
    }


    fun cancelAll() {
        scheduledCommands.forEach(Command::cancel)
        logInfo("canceled all commands")
    }

    fun isScheduled(vararg commands: Command): Boolean {
        return scheduledCommands.containsAll(commands.toList())
    }

    fun requiring(subsystem: Subsystem): Command {
        return scheduledCommandRequirements[subsystem]!!
    }

    fun addPeriodic(action: () -> Unit) {
        InfiniteCommand(action).schedule()
        logInfo("added periodic")
    }

    fun scheduleWatchdog(condition: () -> Boolean, command: Command) {
        schedule(Watchdog(condition, command))
        logInfo("added watchdog ${command.name}")
    }
}
