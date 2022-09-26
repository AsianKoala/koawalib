package com.asiankoala.koawalib.command

import com.asiankoala.koawalib.command.commands.*
import com.asiankoala.koawalib.command.group.Group
import com.asiankoala.koawalib.command.group.ParallelGroup
import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.subsystem.Subsystem
import com.asiankoala.koawalib.util.OpModeState
import com.asiankoala.koawalib.util.disjoint
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashSet
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
    private val scheduledCmds: MutableList<Cmd> = ArrayList()
    private val scheduledCmdReqs: MutableMap<Subsystem, Cmd> = LinkedHashMap()
    private val subsystems: MutableMap<Subsystem, Cmd?> = LinkedHashMap()
    private val toSchedule: MutableList<Cmd> = ArrayDeque()
    private val toCancel: MutableList<Cmd> = ArrayDeque()
    internal val deviceRegistry: MutableMap<String, KDevice<*>> = HashMap()

    private val allCollections = listOf(scheduledCmds, scheduledCmdReqs, subsystems, toSchedule, toCancel, deviceRegistry)
    private val allMaps = listOf<MutableMap<*, *>>(scheduledCmdReqs, subsystems, deviceRegistry)
    private val allLists = listOf<MutableList<*>>(scheduledCmds, toCancel, toSchedule, toCancel)

    private var amountOfWatchdogs = 0

    internal lateinit var stateReceiver: () -> OpModeState

    internal fun resetScheduler() {
        allMaps.forEach(MutableMap<*, *>::clear)
        allLists.forEach(MutableList<*>::clear)
        amountOfWatchdogs = 0

        allCollections.forEach {
            if (it is Collection<*>) {
                if (it.isNotEmpty()) throw Exception("collection not empty on init")
            } else if (it is Map<*, *>) {
                if (it.isNotEmpty()) throw Exception("collection not empty on init")
            }
        }
    }

    private fun initCommand(cmd: Cmd, cRequirements: Set<Subsystem>) {
        cmd.initialize()
        scheduledCmds.add(cmd)
        Logger.logDebug("command ${cmd.name} initialized")
        cRequirements.forEach { scheduledCmdReqs[it] = cmd }
    }

    private fun Cmd.scheduleThis() {
        if (scheduledCmdReqs.keys disjoint requirements) {
            initCommand(this, requirements)
        } else {
            Logger.logWarning("command ${this.name}: Command overlap scheduledRequirementKeys")

            requirements.forEach {
                if (scheduledCmdReqs.containsKey(it)) {
                    val toCancelScheduled = scheduledCmdReqs[it]!!
                    toCancelScheduled.cancel()
                    Logger.logWarning("command ${this.name}: Command caused command ${toCancelScheduled.name} to cancel")
                }
            }

            initCommand(this, requirements)
            Logger.logWarning("command ${this.name}: Command initialized following cancellation of commands with overlapping requirements")
        }
    }

    private fun Cmd.cancelThis() {
        if (!scheduledCmds.contains(this)) {
            return
        }

        this.end()
        Logger.logInfo("command ${this.name} canceled")
        scheduledCmds.remove(this)
        scheduledCmdReqs.keys.removeAll(this.requirements)
        toSchedule.remove(this)
    }

    internal fun update() {
        toSchedule.forEach { it.scheduleThis() }
        toCancel.forEach { it.cancelThis() }

        // @TODO maybe subsytem periodics should be ran before default commands?
        // need to think about this some more
        subsystems.forEach { (k, v) ->
            // if subsystem not required by any command, has a non-null default command, and no
            // scheduled commands have requirements that match the default commands requirements
            if (!scheduledCmdReqs.containsKey(k) && v != null && scheduledCmdReqs.keys disjoint v.requirements) {
                v.execute()
                Logger.logDebug("subsystem ${k.name} default cmd executed")
            }
        }

        toSchedule.clear()
        toCancel.clear()

        subsystems.keys.forEach(Subsystem::periodic)

        // is this logging really needed? // @TODO
//        scheduledCmdReqs.keys.forEachIndexed { i, subsystem ->
//            if (i == 0) Logger.logDebug("required subsystems before running commands:")
//            Logger.logDebug("$i: ${subsystem.name}")
//        }

        val toRemove = LinkedHashSet<Cmd>()
        scheduledCmds.forEach {
            val command = it
            command.execute()

            if (command !is Watchdog && command !is LoopCmd && command !is ParallelGroup) {
                if (command is Group) {
                    Logger.logDebug("group ${command.name} with cmd(s) ${command.currentCmdNames} executed")
                } else {
                    Logger.logDebug("cmd ${command.name} executed")
                }
            }

            if (command.isFinished) {
                command.end()
                if (command !is InstantCmd) Logger.logDebug("command ${command.name} finished")
                toRemove.add(command)
                scheduledCmdReqs.keys.removeAll(command.requirements)
            }
        }

        scheduledCmds.removeAll(toRemove)
    }

    private fun scheduleForState(state: OpModeState, cmd: Cmd) {
        schedule(cmd.waitUntil { stateReceiver.invoke() == state })
    }

    /**
     * Schedule commands
     * @param cmds commands to schedule
     */
    fun schedule(vararg cmds: Cmd) {
        cmds.forEach {
            toSchedule.add(it)
            Logger.logDebug("added ${it.name} to toSchedule array")
        }
    }

    /**
     * Cancel commands, removing them from the scheduler and ending them
     * @param cmds commands to cancel
     */
    fun cancel(vararg cmds: Cmd) {
        toCancel.addAll(cmds)
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
     * @param cmd the default command
     */
    fun setDefaultCommand(subsystem: Subsystem, cmd: Cmd) {
        if (cmd.requirements.size != 1 || subsystem !in cmd.requirements) {
            throw Exception("command ${cmd.name}: default commands must require only subsystem ${subsystem.name}")
        }

        if (cmd.isFinished) {
            throw Exception("command ${cmd.name}: default commands must not end")
        }

        Logger.logInfo("set default command of ${subsystem.name} to ${cmd.name}")
        subsystems[subsystem] = cmd
    }

    /**
     * Get default command of a subsystem
     * @param subsystem queried subsystem
     * @return queried subsystem's default command
     */
    fun getDefaultCommand(subsystem: Subsystem): Cmd {
        return subsystems[subsystem]!!
    }

    /**
     * Get if commands are scheduled
     * @param cmds queried commands
     * @return if all commands are currently scheduled
     */
    fun isScheduled(vararg cmds: Cmd): Boolean {
        return scheduledCmds.containsAll(cmds.toList())
    }

    /**
     * Get the command that is requiring a subsystem
     * @param subsystem queried subsystem
     * @return the command that is requiring the queried subsystem, if it exists
     */
    fun requiring(subsystem: Subsystem): Cmd? {
        return scheduledCmdReqs[subsystem]
    }

    /**
     * Schedule a watchdog
     * @see Watchdog
     * @param condition condition to schedule the watchdog's command
     * @param cmd the watchdog's command
     */
    fun scheduleWatchdog(condition: () -> Boolean, cmd: Cmd) {
        schedule(Watchdog(condition, cmd).withName(cmd.name))
        Logger.logInfo("added watchdog ${cmd.name}")
        amountOfWatchdogs++
    }

    /**
     * Schedule a command upon entering opmode init
     * @param cmd command to be scheduled
     */
    fun scheduleForInit(cmd: Cmd) {
        scheduleForState(OpModeState.INIT, cmd)
    }

    /**
     * Schedule a command upon entering opmode start
     * @param cmd command to be scheduled
     */
    fun scheduleForStart(cmd: Cmd) {
        scheduleForState(OpModeState.START, cmd)
    }

    /**
     * Register a device with KScheduler
     * @see KScheduler
     */
    fun registerDevices(vararg devices: KDevice<*>) {
        devices.forEach {
            Logger.logInfo("registered device ${it.deviceName}")
            deviceRegistry[it.deviceName] = it
        }
    }
}
