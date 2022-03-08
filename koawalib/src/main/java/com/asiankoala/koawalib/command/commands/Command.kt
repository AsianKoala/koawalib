package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.command.CommandScheduler
import com.asiankoala.koawalib.command.group.ParallelCommandGroup
import com.asiankoala.koawalib.command.group.ParallelDeadlineGroup
import com.asiankoala.koawalib.command.group.ParallelRaceGroup
import com.asiankoala.koawalib.command.group.SequentialCommandGroup
import com.asiankoala.koawalib.subsystem.Subsystem

fun interface Command {
    fun init() {}

    fun execute()

    fun end(interrupted: Boolean) {}

    val isFinished: Boolean get() = false

    fun getRequirements(): Set<Subsystem> { return HashSet() }

    fun hasRequirement(requirement: Subsystem): Boolean {
        return getRequirements().contains(requirement)
    }

    // cancels this command based on timeout (seconds)
    fun withTimeout(time: Double): Command {
        return ParallelRaceGroup(this, WaitCommand(time))
    }

    // cancels this command based on a condition
    fun interruptOn(condition: () -> Boolean): Command {
        return ParallelRaceGroup(this, WaitUntilCommand(condition))
    }

    // run an instant command after this command
    fun whenFinished(action: () -> Unit): Command {
        return SequentialCommandGroup(this, InstantCommand(action))
    }

    // run an instant command before this command
    fun beforeStarting(action: () -> Unit): Command {
        return SequentialCommandGroup(InstantCommand(action), this)
    }

    fun continueIf(condition: () -> Boolean): Command {
        return SequentialCommandGroup(this, WaitUntilCommand(condition))
    }

    // run commands sequentially, in a sequential group
    fun andThen(vararg next: Command): Command {
        val group = SequentialCommandGroup(this)
        group.addCommands(*next)
        return group
    }

    fun pauseFor(seconds: Double): Command {
        return SequentialCommandGroup(this, WaitCommand(seconds))
    }

    // run commands in parallel, ends when the current command, the deadline, ends
    fun deadlineWith(vararg parallel: Command): Command {
        return ParallelDeadlineGroup(this, *parallel)
    }

    // run commands parallel, ending when all the commands have ended
    fun alongWith(vararg parallel: Command): Command {
        val group = ParallelCommandGroup(this)
        group.addCommands(*parallel)
        return group
    }

    // run commands parallel, ending when one of the commands have ended
    fun raceWith(vararg parallel: Command): Command {
        val group = ParallelRaceGroup(this)
        group.addCommands(*parallel)
        return group
    }

    fun cancelUpon(condition: () -> Boolean): Command {
        return CancelableCommand(condition, this)
    }

    fun schedule(interruptible: Boolean) {
        CommandScheduler.schedule(interruptible, this)
    }

    fun schedule() {
        schedule(true)
    }

    fun cancel() {
        CommandScheduler.cancel(this)
    }

    val isScheduled: Boolean get() = CommandScheduler.isScheduled(this)

    val name: String get() = this.javaClass.simpleName
}
