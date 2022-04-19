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
     * Returns whether the command is currently scheduled. Syntax sugar for CommandScheduler.isScheduled(command)
     */
    val isScheduled: Boolean get() = CommandScheduler.isScheduled(this)

    /**
     * The name of the command. Used internally to identify logged commands.
     * If the user desires identifying commands in the Logger, specify the name of the command with CommandBase's withName() method
     * @see CommandBase
     */
    val name: String get() = ""

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
    fun cancelIf(condition: () -> Boolean): Command {
        return ParallelRaceGroup(this, WaitUntilCommand(condition))
    }

    /**
     * Runs n commands sequentially after this command
     * @param next n number of commands to run sequentially following this command
     * @return SequentialCommandGroup with this command -> next commands
     */
    fun andThen(vararg next: Command): Command {
        val group = SequentialCommandGroup(this)
        group.addCommands(*next)
        return group
    }

    /**
     * Pause for n seconds after this command ends
     * @param seconds amount of seconds to pause following this command
     * @return SequentialCommandGroup with this command -> WaitCommand
     */
    fun pauseFor(seconds: Double): Command {
        return SequentialCommandGroup(this, WaitCommand(seconds))
    }

    /**
     * Runs n commands in parallel with this command, ending when this command ends
     * @param parallel n number of commands to run in parallel with this command
     * @return ParallelDeadlineGroup with this command as the deadline, with n next commands
     */
    fun deadlineWith(vararg parallel: Command): Command {
        return ParallelDeadlineGroup(this, *parallel)
    }

    /**
     * Run n commands in parallel with this command, ending when all commands have ended
     * @param parallel commands to run in parallel with this command
     * @return ParallelCommandGroup with this command and n parallel commands
     */
    fun alongWith(vararg parallel: Command): Command {
        val group = ParallelCommandGroup(this)
        group.addCommands(*parallel)
        return group
    }

    /**
     * Run n commands in parallel with this command, ending when any of the commands has ended
     * @param parallel commands to run in parallel with this command
     * @return ParallelRaceGroup with this command and n parallel commands
     */
    fun raceWith(vararg parallel: Command): Command {
        val group = ParallelRaceGroup(this)
        group.addCommands(*parallel)
        return group
    }

    /**
     * Schedule this command. Syntax sugar for CommandScheduler.schedule(command)
     * @see CommandScheduler
     */
    fun schedule() {
        CommandScheduler.schedule(this)
    }

    /**
     * Force cancel this command. Syntax sugar for CommandScheduler.cancel()
     * @see CommandScheduler
     */
    fun cancel() {
        CommandScheduler.cancel(this)
    }
}
