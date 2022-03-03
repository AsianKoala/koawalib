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
    private val mScheduledCommands: MutableList<Command> = ArrayList()
    private val mRequirements: MutableMap<Subsystem, Command> = LinkedHashMap()
    private val mSubsystems: MutableMap<Subsystem, Command?> = LinkedHashMap()

    private val mInitActions: MutableList<(Command) -> Unit> = ArrayList()
    private val mExecuteActions: MutableList<(Command) -> Unit> = ArrayList()
    private val mInterruptActions: MutableList<(Command) -> Unit> = ArrayList()
    private val mFinishActions: MutableList<(Command) -> Unit> = ArrayList()

    private val mToSchedule: MutableList<Command> = ArrayList()
    private val mToCancel: MutableList<Command> = ArrayList()

    private val allMaps = listOf(mRequirements, mSubsystems)
    private val allLists = listOf(
        mInitActions, mExecuteActions,
        mInterruptActions, mFinishActions, mToCancel, mToSchedule
    )

    private var mDisabled = false
    private var mInRunLoop = false

    private lateinit var mOpMode: CommandOpMode

    private var runningOpMode = false

    val isOpModeLooping get() = mOpMode.isLooping

    private fun initCommand(command: Command, requirements: Set<Subsystem>) {
        command.init()
        mScheduledCommands.add(command)
        mInitActions.forEach { it.invoke(command) }
        requirements.forEach { mRequirements[it] = command }
    }

    private fun schedule(command: Command) {
        if (mInRunLoop) {
            mToSchedule
            return
        }

        if (CommandGroupBase.getGroupedCommands().contains(command)) {
            throw IllegalArgumentException("A command that is part of a command group cannot be independently scheduled")
        }

        if (runningOpMode && (
            mDisabled || (!command.runsWhenDisabled && mOpMode.disabled) ||
                mScheduledCommands.contains(command)
            )
        ) {
            return
        }

        val requirements = command.getRequirements()

        if (Collections.disjoint(mRequirements.keys, requirements)) {
            initCommand(command, requirements)
        } else {
            requirements.forEach {
                if (mRequirements.containsKey(it)) {
                    mRequirements[it]!!.cancel()
                }
            }

            initCommand(command, requirements)
        }
    }

    fun resetScheduler() {
        allMaps.forEach(MutableMap<*, *>::clear)
        allLists.forEach(MutableList<*>::clear)
        mDisabled = false
        mInRunLoop = false
    }

    fun printTelemetry() {
        println()
        println()
        println("scheduled command size ${mScheduledCommands.size}")
        println("requirements size ${mRequirements.size}")
        println("subsystems size ${mSubsystems.size}")
        println("to schedule size ${mToSchedule.size} ")
        println("to cancel size ${mToCancel.size}")
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
        if (mDisabled) {
            return
        }

        mSubsystems.keys.forEach(Subsystem::periodic)

        mInRunLoop = true
        val iterator = mScheduledCommands.iterator()
        while (iterator.hasNext()) {
            val command = iterator.next()

            if (runningOpMode) {
                if (!command.runsWhenDisabled && mOpMode.disabled) {
                    command.end(true)
                    mInterruptActions.forEach { it.invoke(command) }
                    mRequirements.keys.removeAll(command.getRequirements())
                    iterator.remove()
                    return
                }

                if (!command.runsWhenDisabled && mOpMode.disabled) {
                    command.cancel()
                    iterator.remove()
                    return
                }
            }

            command.execute()
            mExecuteActions.forEach { it.invoke(command) }

            if (command.isFinished) {
                command.end(false)
                mFinishActions.forEach { it.invoke(command) }
                iterator.remove()
                mRequirements.keys.removeAll(command.getRequirements())
            }
        }

        mInRunLoop = false

        mToSchedule.forEach(Command::schedule)
        mToCancel.forEach(Command::cancel)

        mToSchedule.clear()
        mToCancel.clear()

        mSubsystems.forEach { (k, v) ->
            if (!mRequirements.containsKey(k) && v != null) {
                schedule(v)
            }
        }
    }

    fun registerSubsystem(vararg subsystems: Subsystem) {
        subsystems.forEach { mSubsystems[it] = null }
    }

    fun unregisterSubsystem(vararg subsystems: Subsystem) {
        mSubsystems.keys.removeAll(subsystems)
    }

    fun setDefaultCommand(subsystem: Subsystem, command: Command) {
        if (!command.getRequirements().contains(subsystem)) {
            throw IllegalArgumentException("Default commands must require subsystem")
        }

        if (command.isFinished) {
            throw java.lang.IllegalArgumentException("Default commands should not end")
        }

        mSubsystems[subsystem] = command
    }

    fun getDefaultCommand(subsystem: Subsystem): Command {
        return mSubsystems[subsystem]!!
    }

    fun cancel(vararg commands: Command) {
        if (mInRunLoop) {
            mToCancel.addAll(commands)
            return
        }

        commands.forEach {
            if (!mScheduledCommands.contains(it)) {
                return@forEach
            }

            it.end(true)
            mInterruptActions.forEach { action -> action.invoke(it) }
            mScheduledCommands.remove(it)
            mRequirements.keys.removeAll(it.getRequirements())
        }
    }

    fun cancelAll() {
        mScheduledCommands.forEach(Command::cancel)
    }

    fun isScheduled(vararg commands: Command): Boolean {
        return mScheduledCommands.containsAll(commands.toList())
    }

    fun requiring(subsystem: Subsystem): Command {
        return mRequirements[subsystem]!!
    }

    fun disable() {
        mDisabled = true
    }

    fun enable() {
        mDisabled = false
    }

    fun addPeriodic(action: () -> Unit) {
        InfiniteCommand(action).schedule()
    }

    fun scheduleWatchdog(condition: () -> Boolean, command: Command) {
        schedule(Watchdog(condition, command))
    }

    fun onCommandInit(action: (Command) -> Unit) {
        mInitActions.add(action)
    }

    fun onCommandExecute(action: (Command) -> Unit) {
        mExecuteActions.add(action)
    }

    fun onCommandInterrupt(action: (Command) -> Unit) {
        mInterruptActions.add(action)
    }

    fun onCommandFinish(action: (Command) -> Unit) {
        mFinishActions.add(action)
    }
}
