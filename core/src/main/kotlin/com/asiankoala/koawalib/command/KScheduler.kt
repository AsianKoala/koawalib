package com.asiankoala.koawalib.command

import com.asiankoala.koawalib.command.commands.*
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.subsystem.KSubsystem
import com.asiankoala.koawalib.util.OpModeState
import com.asiankoala.koawalib.util.internal.disjoint
import kotlin.collections.set

/**
 * KScheduler runs all commands. Every loop the scheduler checks for newly scheduled commands, cancelled commands, finished commands,
 * and handles them accordingly. Processing is done behind the scenes, so the main purpose of this class for the user
 * is to schedule commands, mainly using [KScheduler.schedule]
 */
object KScheduler {
    private val cmds: MutableMap<Cmd, Set<KSubsystem>> = LinkedHashMap()
    private val subsystems: MutableList<KSubsystem> = ArrayDeque()
    private val toSchedule: MutableList<Cmd> = ArrayDeque()
    private val toCancel: MutableList<Cmd> = ArrayDeque()
    private val allLists = listOf<MutableList<*>>(subsystems, toSchedule, toCancel)

    internal lateinit var stateReceiver: () -> OpModeState

    internal fun resetScheduler() {
        cmds.clear()
        allLists.forEach(MutableList<*>::clear)
    }

    private fun Cmd.scheduleThis() {
        cmds.filter { !(requirements disjoint it.value) }
            .keys
            .forEach(Cmd::cancel)

        this.initialize()
        cmds[this] = requirements
        Logger.logDebug("command $name initialized")
    }

    private fun Cmd.cancelThis() {
        toSchedule.remove(this)
        if (this !in cmds) return
        this.end()
        Logger.logInfo("command ${this.name} canceled")
        cmds.remove(this)
    }

    internal fun update() {
        toSchedule.forEach { it.scheduleThis() }
        toCancel.forEach { it.cancelThis() }

        val f = cmds.values.flatten()
        subsystems.filter { it !in f && it.defaultCommand != null }
            .forEach { it.defaultCommand!!.execute() }

        toSchedule.clear()
        toCancel.clear()

        subsystems.forEach(KSubsystem::periodic)
        val toRemove = LinkedHashSet<Cmd>()
        cmds.forEach {
            val command = it.key
            command.execute()
            if (command.isFinished) {
                command.end()
                toRemove.add(command)
            }
        }

        toRemove.forEach { cmds.remove(it) }
    }

    /**
     * Schedule commands
     * @param cmds commands to schedule
     */
    fun schedule(vararg cmds: Cmd) {
        toSchedule.addAll(cmds)
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
    fun registerSubsystem(vararg requestedSubsystems: KSubsystem) {
        requestedSubsystems.forEach { Logger.logInfo("registered subsystem ${it.name}") }
        subsystems.addAll(requestedSubsystems.toSet())
    }

    /**
     * Unregister subsystems
     * @param requestedSubsystems subsystems to unregister
     */
    fun unregisterSubsystem(vararg requestedSubsystems: KSubsystem) {
        requestedSubsystems.forEach { Logger.logInfo("unregistered subsystem ${it.name}") }
        subsystems.removeAll(requestedSubsystems.toSet())
    }
}
