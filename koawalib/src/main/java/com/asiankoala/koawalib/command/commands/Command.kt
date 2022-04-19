package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.command.CommandScheduler
import com.asiankoala.koawalib.command.group.ParallelCommandGroup
import com.asiankoala.koawalib.command.group.ParallelDeadlineGroup
import com.asiankoala.koawalib.command.group.ParallelRaceGroup
import com.asiankoala.koawalib.command.group.SequentialCommandGroup
import com.asiankoala.koawalib.subsystem.Subsystem

/**
 * Commands are the basis of how koawalib interacts with the robot.
 * Each command has initialize(), execute(), and end() methods executed throughout its lifecycle.
 * Commands also contain a list of subsystem "requirements".
 * This prevents multiple commands from accessing the same subsystem simultaneously.
 * To create custom commands, extend the CommandBase class.
 * All commands are scheduled and ran through the CommandScheduler.
 *
 * @see CommandScheduler
 * @see CommandBase
 */
fun interface Command {
    /**
     * The first part of running a command. initialize() is internally called at the very beginning of a command's lifestyle, right when it is scheduled
     */
    fun initialize() {}

    /**
     * The main part of a command. Until the command is finished, execute() will be continuously called
     */
    fun execute()

    /**
     * The final part of a command. end() will be called once when the command has finished
     */
    fun end() {}

    /**
     * Returns whether the command has finished or not
     */
    val isFinished: Boolean get() = true

    /**
     * @return a set containing the command's subsystem requirements
     */
    fun getRequirements(): Set<Subsystem> { return HashSet() }

    /**
     * @param requirement queried subsystem requirement
     * @return whether or not the command contains the queried subsystem as part of it's requirements
     */
    fun hasRequirement(requirement: Subsystem): Boolean {
        return getRequirements().contains(requirement)
    }

    /**
     * Wait until a condition has been fulfilled to run this command
     * @param condition condition to allow the start of this command
     * @return SequentialCommandGroup with a WaitUntilCommand -> this command
     */
    fun waitUntil(condition: () -> Boolean): Command {
        return SequentialCommandGroup(WaitUntilCommand(condition), this)
    }

    /**
     * Cancels this command after some time if not finished
     * @param time the timeout duration. units are seconds
     * @return ParallelRaceGroup with a WaitCommand & this command
     */
    fun withTimeout(time: Double): Command {
        return ParallelRaceGroup(this, WaitCommand(time))
    }

    /**
     * Cancels this command upon fulfilling a condition
     * @param condition the condition that ends the current command
     * @return ParallelRaceGroup with a WaitUntilCommand & this command
     */
    fun interruptOn(condition: () -> Boolean): Command {
        return ParallelRaceGroup(this, WaitUntilCommand(condition))
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

    // todo fix canceling
    // todo fix canceling
    // todo fix canceling
    // todo fix canceling
    // todo fix canceling
    // todo fix canceling
    fun cancelUpon(condition: () -> Boolean): Command {
        return CancelableCommand(condition, this)
    }

    fun schedule() {
        CommandScheduler.schedule(this)
    }

    fun cancel() {
        CommandScheduler.cancel(this)
    }

    val isScheduled: Boolean get() = CommandScheduler.isScheduled(this)

    val name: String get() = ""
}
