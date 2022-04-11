package com.asiankoala.koawalib.command

import com.asiankoala.koawalib.command.commands.*
import com.asiankoala.koawalib.command.group.CommandGroupBase
import com.asiankoala.koawalib.subsystem.Subsystem
import com.asiankoala.koawalib.util.Logger
import com.asiankoala.koawalib.util.OpModeState
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

@Suppress("unused")
object CommandScheduler {
    private val scheduledCommands: MutableList<Command> = ArrayList()
    private val scheduledCommandRequirements: MutableMap<Subsystem, Command> = LinkedHashMap()
    private val subsystems: MutableMap<Subsystem, Command?> = LinkedHashMap()
    private val toSchedule: MutableList<Command> = ArrayDeque()
    private val toCancel: MutableList<Command> = ArrayDeque()

    private val allMaps = listOf<MutableMap<*, *>>(scheduledCommandRequirements, subsystems)
    private val allLists = listOf<MutableList<*>>(scheduledCommands, toCancel, toSchedule, toCancel)

    private var amountOfWatchdogs = 0

    internal lateinit var opModeInstance: CommandOpMode

    internal fun resetScheduler() {
        allMaps.forEach(MutableMap<*, *>::clear)
        allLists.forEach(MutableList<*>::clear)
        amountOfWatchdogs = 0
    }

    private fun initCommand(command: Command, cRequirements: Set<Subsystem>) {
        command.initialize()
        scheduledCommands.add(command)
        Logger.logDebug("command ${command.name} initialized")
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
            Logger.logWarning("command ${this.name}: Command overlap scheduledRequirementKeys")

            requirements.forEach {
                if (scheduledCommandRequirements.containsKey(it)) {
                    val toCancelScheduled = scheduledCommandRequirements[it]!!
                    toCancelScheduled.cancel()
                    Logger.logWarning("command ${this.name}: Command caused command ${toCancelScheduled.name} to cancel")
                }
            }

            initCommand(this, requirements)
            Logger.logWarning("command ${this.name}: Command initialized following cancellation of commands with overlapping requirements")
        }
    }

    private fun Command.cancelThis() {
        if (!scheduledCommands.contains(this)) {
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

        toSchedule.forEach { it.scheduleThis() }
        toCancel.forEach { it.cancelThis() }

        subsystems.forEach { (k, v) ->
            if (!scheduledCommandRequirements.containsKey(k) && v != null && Collections.disjoint(
                    scheduledCommandRequirements.keys, v.getRequirements()
                )
            ) {
                v.execute()
            }
        }

        toSchedule.clear()
        toCancel.clear()

        subsystems.keys.forEach(Subsystem::periodic)

        Logger.logDebug("required subsystems before running commands:")
        scheduledCommandRequirements.keys.forEachIndexed { i, subsystem ->
            Logger.logDebug("$i: ${subsystem.name}")
        }

        Logger.logDebug("number of commands (excluding watchdog): ${scheduledCommands.size - amountOfWatchdogs}")

        val iterator = scheduledCommands.iterator()
        while (iterator.hasNext()) {
            val command = iterator.next()

            command.execute()

//            if(command !is Watchdog && command !is InfiniteCommand && command !is CommandGroupBase) {
//                Logger.logInfo("${command.name} executed")
//            }

            if (command.isFinished) {
                command.end(false)
                Logger.logDebug("command ${command.name} finished")
                iterator.remove()
                scheduledCommandRequirements.keys.removeAll(command.getRequirements())
            }
        }
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

    fun registerSubsystem(vararg requestedSubsystems: Subsystem) {
        requestedSubsystems.forEach {
            Logger.logInfo("registered subsystem ${it.name}")
            this.subsystems[it] = null
        }
    }

    fun unregisterSubsystem(vararg requestedSubsystems: Subsystem) {
        requestedSubsystems.forEach { Logger.logInfo("unregistered subsystem ${it.name}") }
        this.subsystems.keys.removeAll(requestedSubsystems)
    }

    fun setDefaultCommand(subsystem: Subsystem, command: Command) {
        if (!command.getRequirements().contains(subsystem)) {
            Logger.logError("command ${command.name}: default commands must require subsystem")
        }

        if (command.getRequirements().size != 1) {
            Logger.logError("command ${command.name}: default commands must only require one subsystem")
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
        schedule(Watchdog(condition, command).withName(command.name))
        Logger.logInfo("added watchdog ${command.name}")
        amountOfWatchdogs++
    }

    fun scheduleForState(state: OpModeState, command: Command) {
        schedule(command.waitUntil { opModeInstance.opmodeState == state })
    }

    fun scheduleForInit(command: Command) {
        scheduleForState(OpModeState.INIT, command)
    }

    fun scheduleForStart(command: Command) {
        scheduleForState(OpModeState.START, command)
    }

}
