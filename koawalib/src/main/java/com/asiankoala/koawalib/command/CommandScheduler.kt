package com.asiankoala.koawalib.command

import com.asiankoala.koawalib.command.commands.*
import com.asiankoala.koawalib.command.group.CommandGroupBase
import com.asiankoala.koawalib.subsystem.Subsystem
import com.asiankoala.koawalib.util.Logger
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.Set
import kotlin.collections.addAll
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach
import kotlin.collections.listOf
import kotlin.collections.removeAll
import kotlin.collections.set
import kotlin.collections.toList

// TODO: INTERNAL SHIT INSTEAD OF MAKING THEM PUBLIC :RAGE:
// TODO: ASSERT NOT CHANGING PER LOOP OR SOMETHING? SOUNDS COOL
@Suppress("unused")
object CommandScheduler {
    private val scheduledCommands: MutableList<Command> = ArrayList()
    private val scheduledCommandRequirements: MutableMap<Subsystem, Command> = LinkedHashMap()
    private val subsystems: MutableMap<Subsystem, Command?> = LinkedHashMap()

    private val toSchedule: MutableList<Command> = ArrayDeque()
    private val toCancel: MutableList<Command> = ArrayDeque()

    private val loopAssertionMap: MutableMap<Any, Int> = HashMap()

    private val allMaps = listOf<MutableMap<*,*>>(scheduledCommandRequirements, subsystems)
    private val allLists = listOf<MutableList<*>>(scheduledCommands, toCancel, toSchedule, toCancel)

    internal var isOpModeLooping = false
        private set

    internal fun resetScheduler() {
        allMaps.forEach(MutableMap<*, *>::clear)
        allLists.forEach(MutableList<*>::clear)
        isOpModeLooping = false
    }

    private fun initCommand(command: Command, cRequirements: Set<Subsystem>) {
        command.init()
        scheduledCommands.add(command)
        Logger.logInfo("command ${command.name} initialized")
        cRequirements.forEach { scheduledCommandRequirements[it] = command }
    }

    private fun Command.scheduleThis() {
        if (CommandGroupBase.getGroupedCommands().contains(this)) {
            Logger.logError("command ${this.name}: Command in command groups cannot be independently scheduled")
        } else {
            Logger.logDebug("command ${this.name}: Command not in any command groups")
        }

        val requirements = this.getRequirements()

        if (Collections.disjoint(scheduledCommandRequirements.keys, requirements)) {
            initCommand(this, requirements)
            Logger.logDebug("command ${this.name}: Command disjoint with scheduledRequirementKeys")
        } else {
            requirements.forEach {
                if (scheduledCommandRequirements.containsKey(it)) {
                    val scheduled = scheduledCommandRequirements[it]!!
                    scheduled.cancel()
                    Logger.logWarning("command ${this.name}: Command caused command ${scheduled.name} to cancel")
                }
            }

            initCommand(this, requirements)
            Logger.logDebug("command ${this.name}: Command initialized following cancellation of commands with overlapping requirements")
        }
    }

    private fun Command.cancelThis() {
        if(!scheduledCommands.contains(this)) {
            return
        }

        this.end(true)
        Logger.logInfo("command ${this.name} canceled")
        scheduledCommands.remove(this)
        scheduledCommandRequirements.keys.removeAll(this.getRequirements())
    }

    internal fun run() {
        Logger.logDebug("CommandScheduler entered run()")
        Logger.logDebug("amount of scheduled commands before run(): ${scheduledCommands.size + toSchedule.size}")

        loopAssertionMap.forEach { (k, v) ->
            if(v > 1) {
                Logger.logWarning("$k repeated $v times")
            }
            loopAssertionMap[k] = 0
        }

        toSchedule.forEach { it.scheduleThis() }
        toCancel.forEach { it.cancelThis() }

        toSchedule.clear()
        toCancel.clear()

        subsystems.keys.forEach(Subsystem::periodic)

        val iterator = scheduledCommands.iterator()
        while (iterator.hasNext()) {
            val command = iterator.next()

            command.execute()
            Logger.logInfo("command ${command.name} executed")

            if (command.isFinished) {
                command.end(false)
                Logger.logInfo("command ${command.name} finished")
                iterator.remove()
                scheduledCommandRequirements.keys.removeAll(command.getRequirements())
            }
        }

        subsystems.forEach { (k, v) ->
            if (!scheduledCommandRequirements.containsKey(k) && v != null) {
                schedule(v)
            }
        }

        Logger.logDebug("amount of scheduled commands after run(): ${scheduledCommands.size}")
        Logger.logDebug("CommandScheduler exited run()")
    }

    fun schedule(vararg commands: Command) {
        commands.forEach {
            toSchedule.add(it)
            Logger.logDebug("added ${it.name} to toSchedule array")
        }
    }

    fun cancel(vararg commands: Command) {
        toCancel.addAll(commands)
    }

    fun startOpModeLooping() {
        isOpModeLooping = true
    }

    fun registerSubsystem(vararg requestedSubsystems: Subsystem) {
        requestedSubsystems.forEach { this.subsystems[it] = null }
    }

    fun unregisterSubsystem(vararg requestedSubsystems: Subsystem) {
        requestedSubsystems.forEach { Logger.logInfo("unregistered subsystem ${it.name}") }
        this.subsystems.keys.removeAll(requestedSubsystems)
    }

    fun setDefaultCommand(subsystem: Subsystem, command: Command) {
        if (!command.getRequirements().contains(subsystem)) {
            Logger.logError("command ${command.name}: default commands must require subsystem")
        }

        if (command.isFinished) {
            Logger.logError("command ${command.name}: default commands must not end")
        }

        Logger.logInfo("set default command of ${subsystem.name} to ${command.name}")
        subsystems[subsystem] = command
    }

    fun getDefaultCommand(subsystem: Subsystem): Command {
        return subsystems[subsystem]!!
    }

    fun cancelAll() {
        scheduledCommands.forEach(Command::cancel)
        Logger.logInfo("canceled all commands")
    }

    fun isScheduled(vararg commands: Command): Boolean {
        return scheduledCommands.containsAll(commands.toList())
    }

    fun requiring(subsystem: Subsystem): Command {
        return scheduledCommandRequirements[subsystem]!!
    }

    fun scheduleWatchdog(condition: () -> Boolean, command: Command) {
        schedule(Watchdog(condition, command))
        Logger.logInfo("added watchdog ${command.name}")
    }

    fun assertUniqueLoop(thing: Any) {
        if(thing in loopAssertionMap.keys) {
            loopAssertionMap[thing] = loopAssertionMap[thing]!! + 1
        } else {
            loopAssertionMap[thing] = 0
        }
    }
}
