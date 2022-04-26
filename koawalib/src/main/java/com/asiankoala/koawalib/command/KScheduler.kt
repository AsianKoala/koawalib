package com.asiankoala.koawalib.command

import com.asiankoala.koawalib.command.commands.*
import com.asiankoala.koawalib.command.group.ParallelGroup
import com.asiankoala.koawalib.subsystem.Subsystem
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.util.OpModeState
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * CommandScheduler runs all commands. Every loop the scheduler checks for newly scheduled commands, cancelled commands, finished commands,
 * and handles them accordingly. Processing is done behind the scenes, so the main purpose of this class for the user
 * is to schedule commands, mainly using [KScheduler.schedule]
 */
@Suppress("unused")
object KScheduler {
    private val scheduledCommands: MutableList<Command> = ArrayList()
    private val scheduledCommandRequirements: MutableMap<Subsystem, Command> = LinkedHashMap()
    private val subsystems: MutableMap<Subsystem, Command?> = LinkedHashMap()
    private val toSchedule: MutableList<Command> = ArrayDeque()
    private val toCancel: MutableList<Command> = ArrayDeque()

    private val allMaps = listOf<MutableMap<*, *>>(scheduledCommandRequirements, subsystems)
    private val allLists = listOf<MutableList<*>>(scheduledCommands, toCancel, toSchedule, toCancel)

    private var amountOfWatchdogs = 0

    internal lateinit var opModeInstance: KOpMode

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

        this.end()
        Logger.logInfo("command ${this.name} canceled")
        scheduledCommands.remove(this)
        scheduledCommandRequirements.keys.removeAll(this.requirements)
        toSchedule.remove(this)
    }

    internal fun run() {
        Logger.logDebug("CommandScheduler entered run()")
        Logger.logDebug("amount of scheduled commands before run(): ${scheduledCommands.size + toSchedule.size}")

        toSchedule.forEach { it.scheduleThis() }

        // todo fix canceling
        // todo fix canceling
        // todo fix canceling
        // todo fix canceling
        // todo fix canceling
        // todo fix canceling
        toCancel.forEach { it.cancelThis() }

        subsystems.forEach { (k, v) ->
            if (!scheduledCommandRequirements.containsKey(k) && v != null && Collections.disjoint(
                    scheduledCommandRequirements.keys, v.requirements
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

            if(command !is Watchdog && command !is LoopCmd && command !is ParallelGroup) {
                Logger.logDebug("${command.name} executed")
            }

            if (command.isFinished) {
                command.end()
                Logger.logDebug("command ${command.name} finished")
                iterator.remove()
                scheduledCommandRequirements.keys.removeAll(command.requirements)
            }
        }
    }

    private fun scheduleForState(state: OpModeState, command: Command) {
        schedule(command.waitUntil { opModeInstance.opmodeState == state })
    }

    /**
     * Schedule commands
     * @param commands commands to schedule
     */
    fun schedule(vararg commands: Command) {
        commands.forEach {
            toSchedule.add(it)
            Logger.logDebug("added ${it.name} to toSchedule array")
        }
    }

    /**
     * Cancel commands, removing them from the scheduler and ending them
     * @param commands commands to cancel
     */
    fun cancel(vararg commands: Command) {
        toCancel.addAll(commands)
    }

    /**
     * Register n subsystems
     * @param requestedSubsystems subsystems to register
     */
    fun registerSubsystem(vararg requestedSubsystems: Subsystem) {
        requestedSubsystems.forEach {
            Logger.logInfo("registered subsystem ${it.name}")
            this.subsystems[it] = null
        }
    }

    /**
     * Unregister subsystems
     * @param requestedSubsystems subsystems to unregister
     */
    fun unregisterSubsystem(vararg requestedSubsystems: Subsystem) {
        requestedSubsystems.forEach { Logger.logInfo("unregistered subsystem ${it.name}") }
        this.subsystems.keys.removeAll(requestedSubsystems)
    }

    /**
     * Set the default command of a subsystem. Default commands run when no other command requires the specified subsystem
     * @param subsystem subsystem to set default command of
     * @param command the default command
     */
    fun setDefaultCommand(subsystem: Subsystem, command: Command) {
        if (!command.requirements.contains(subsystem)) {
            Logger.logError("command ${command.name}: default commands must require subsystem")
        }

        if (command.requirements.size != 1) {
            Logger.logError("command ${command.name}: default commands must only require one subsystem")
        }

        if (command.isFinished) {
            Logger.logError("command ${command.name}: default commands must not end")
        }

        Logger.logInfo("set default command of ${subsystem.name} to ${command.name}")
        subsystems[subsystem] = command
    }

    /**
     * Get default command of a subsystem
     * @param subsystem queried subsystem
     * @return queried subsystem's default command
     */
    fun getDefaultCommand(subsystem: Subsystem): Command {
        return subsystems[subsystem]!!
    }

    /**
     * Get if commands are scheduled
     * @param commands queried commands
     * @return if all commands are currently scheduled
     */
    fun isScheduled(vararg commands: Command): Boolean {
        return scheduledCommands.containsAll(commands.toList())
    }

    /**
     * Get the command that is requiring a subsystem
     * @param subsystem queried subsystem
     * @return the command that is requiring the queried subsystem, if it exists
     */
    fun requiring(subsystem: Subsystem): Command? {
        return scheduledCommandRequirements[subsystem]
    }

    /**
     * Schedule a watchdog
     * @see Watchdog
     * @param condition condition to schedule the watchdog's command
     * @param command the watchdog's command
     */
    fun scheduleWatchdog(condition: () -> Boolean, command: Command) {
        schedule(Watchdog(condition, command).withName(command.name))
        Logger.logInfo("added watchdog ${command.name}")
        amountOfWatchdogs++
    }

    /**
     * Schedule a command upon entering opmode init
     * @param command command to be scheduled
     */
    fun scheduleForInit(command: Command) {
        scheduleForState(OpModeState.INIT, command)
    }

    /**
     * Schedule a command upon entering opmode start
     * @param command command to be scheduled
     */
    fun scheduleForStart(command: Command) {
        scheduleForState(OpModeState.START, command)
    }
}
