package com.asiankoala.koawalib.command

import android.util.Log
import com.asiankoala.koawalib.command.commands.Command
import com.asiankoala.koawalib.command.commands.InfiniteCommand
import com.asiankoala.koawalib.command.commands.LogCommand
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

    private val toSchedule: MutableList<Command> = ArrayDeque()
    private val toCancel: MutableList<Command> = ArrayDeque()
    private val toLog: MutableList<LogCommand> = ArrayDeque()

    private val allMaps = listOf(scheduledCommandRequirements, subsystems)
    private val allLists = listOf(scheduledCommands, toCancel, toSchedule, toLog)

    private var isLogging = true
    private lateinit var mOpMode: CommandOpMode

    val isOpModeLooping get() = mOpMode.isLooping

    private fun initCommand(command: Command, cRequirements: Set<Subsystem>) {
        command.init()
        scheduledCommands.add(command)
        logInfo("command ${command.name} initialized")
        cRequirements.forEach { scheduledCommandRequirements[it] = command }
    }

    private fun schedule(command: Command) {
        toSchedule.add(command)
        logDebug("added ${command.name} to toSchedule array")
    }

    private fun Command.scheduleThis() {
        if (CommandGroupBase.getGroupedCommands().contains(this)) {
            logError("Commands in command groups cannot be independently scheduled")
            throw IllegalArgumentException("Commands in command groups cannot be independently scheduled")
        }

        val requirements = this.getRequirements()

        if (Collections.disjoint(scheduledCommandRequirements.keys, requirements)) {
            initCommand(this, requirements)
            logDebug("command ${this.name} disjoint with scheduledRequirementKeys")
        } else {
            requirements.forEach {
                if (scheduledCommandRequirements.containsKey(it)) {
                    val scheduled = scheduledCommandRequirements[it]!!
                    scheduled.cancel()
                    logWarning("command ${this.name} caused command ${scheduled.name} to cancel")
                }
            }

            initCommand(this, requirements)
            logDebug("initialized command ${this.name} following cancellation of commands with overlapping requirements")
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
        if(isLogging) {
            runLogCommands()
        }

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

        toSchedule.forEach { it.scheduleThis() }
        toCancel.forEach { it.cancelThis() }

        toSchedule.clear()
        toCancel.clear()

        subsystems.forEach { (k, v) ->
            if (!scheduledCommandRequirements.containsKey(k) && v != null) {
                schedule(v)
            }
        }
    }

    fun runLogCommands() {
        val logIterator = toLog.iterator()
        while(logIterator.hasNext()) {
            val logCommand = logIterator.next()
            logCommand.init()
            logCommand.execute()
            logCommand.end(false)
        }
        toLog.clear()
    }

    fun schedule(vararg commands: Command) {
        schedule(*commands)
    }

    fun cancel(vararg commands: Command) {
        toCancel.addAll(commands)
    }

    fun resetScheduler() {
        allMaps.forEach(MutableMap<*, *>::clear)
        allLists.forEach(MutableList<*>::clear)
    }

    fun setOpMode(opMode: CommandOpMode) {
        mOpMode = opMode
        logDebug("set command scheduler op mode")
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
            throw IllegalArgumentException("Default commands must require subsystem")
        }

        if (command.isFinished) {
            logError("tried to set command ${command.name} to default for $subsystem")
            throw java.lang.IllegalArgumentException("Default commands should not end")
        }

        logInfo("set default command of ${subsystem.name} to ${command.name}")
        subsystems[subsystem] = command
    }

    fun getDefaultCommand(subsystem: Subsystem): Command {
        return subsystems[subsystem]!!
    }

    fun log(message: String, priority: Int) {
        toLog.add(LogCommand(message, priority))
    }

    fun logDebug(message: String) {
        log(message, Log.DEBUG)
    }

    fun logInfo(message: String) {
        log(message, Log.INFO)
    }

    fun logWarning(message: String) {
        log(message, Log.WARN)
    }

    fun logError(message: String) {
        log(message, Log.ERROR)
    }

    fun logWTF(message: String) {
        log(message, Log.ASSERT)
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
