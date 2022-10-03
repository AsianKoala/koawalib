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
//@Suppress("unused")
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

    internal lateinit var stateReceiver: () -> OpModeState

    internal fun resetScheduler() {
        allMaps.forEach(MutableMap<*, *>::clear)
        allLists.forEach(MutableList<*>::clear)

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

        subsystems.forEach { (k, v) ->
            if (!scheduledCmdReqs.containsKey(k) && v != null && scheduledCmdReqs.keys disjoint v.requirements) {
                v.execute()
                Logger.logDebug("subsystem ${k.name} default cmd executed")
            }
        }

        toSchedule.clear()
        toCancel.clear()

        subsystems.keys.forEach(Subsystem::periodic)

        val toRemove = LinkedHashSet<Cmd>()
        scheduledCmds.forEach {
            val command = it
            command.execute()

            if (command.isFinished) {
                command.end()
                toRemove.add(command)
                scheduledCmdReqs.keys.removeAll(command.requirements)
            }
        }

        scheduledCmds.removeAll(toRemove)
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
     * Schedule command for a state
     * @param state state to start cmd
     * @param cmd cmd to run when state reached
     */
    fun scheduleForState(state: OpModeState, cmd: Cmd) {
        schedule(cmd.waitUntil { stateReceiver.invoke() == state })
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
     * Schedule a watchdog
     * @see Watchdog
     * @param condition condition to schedule the watchdog's command
     * @param cmd the watchdog's command
     */
    fun scheduleWatchdog(condition: () -> Boolean, cmd: Cmd) {
        schedule(Watchdog(condition, cmd).withName(cmd.name))
        Logger.logInfo("added watchdog ${cmd.name}")
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
