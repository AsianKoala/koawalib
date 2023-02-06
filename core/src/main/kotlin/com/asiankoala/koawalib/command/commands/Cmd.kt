package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.command.KScheduler
import com.asiankoala.koawalib.command.group.DeadlineGroup
import com.asiankoala.koawalib.command.group.ParallelGroup
import com.asiankoala.koawalib.command.group.RaceGroup
import com.asiankoala.koawalib.command.group.SequentialGroup
import com.asiankoala.koawalib.subsystem.KSubsystem

/**
 * Commands are the basis of how koawalib interacts with the robot.
 * Each command has initialize(), execute(), and end() methods executed throughout its lifecycle.
 * Commands contain a list of subsystem "requirements", preventing multiple subsystems accessing a command simultaneously.
 * All commands are scheduled and ran through the CommandScheduler.
 * @see KScheduler
 */
abstract class Cmd {
    private var _name: String? = null
    internal val requirements: MutableSet<KSubsystem> = HashSet()

    /**
     * Finish state of command
     */
    open val isFinished: Boolean get() = false

    /**
     * The name of the command
     */
    val name: String get() = _name ?: this.javaClass.simpleName

    protected fun addRequirements(vararg subsystems: KSubsystem) {
        requirements.addAll(subsystems)
    }

    /**
     * First part of running a command, called on start of command.
     */
    open fun initialize() {}

    /**
     * Main part of a command, called while command is running.
     */
    abstract fun execute()

    /**
     * Final part of a command, called when command finishes.
     */
    open fun end() {}

    /**
     * Wait until a condition has been fulfilled to run this command
     * @param condition condition to allow the start of this command
     * @return SequentialCommandGroup with a WaitUntilCommand -> this command
     */
    fun waitUntil(condition: () -> Boolean) = SequentialGroup(WaitUntilCmd(condition), this)

    /**
     * Cancels this command after some time if not finished
     * @param time the timeout duration. units are seconds
     * @return ParallelRaceGroup with a WaitCommand & this command
     */
    fun withTimeout(time: Double) = RaceGroup(this, WaitCmd(time))

    /**
     * Cancels this command upon fulfilling a condition
     * @param condition the condition that ends the current command
     * @return ParallelRaceGroup with a WaitUntilCommand & this command
     */
    fun cancelIf(condition: () -> Boolean) = RaceGroup(this, WaitUntilCmd(condition))

    /**
     * Runs n commands sequentially after this command
     * @param next n number of commands to run sequentially following this command
     * @return SequentialCommandGroup with this command -> next commands
     */
    fun andThen(vararg next: Cmd) = SequentialGroup(this, *next)

    /**
     * Pause for n seconds after this command ends
     * @param seconds amount of seconds to pause following this command
     * @return SequentialCommandGroup with this command -> WaitCommand
     */
    fun andPause(seconds: Double) = SequentialGroup(this, WaitCmd(seconds))

    /**
     * Runs n commands in parallel with this command, ending when this command ends
     * @param parallel n number of commands to run in parallel with this command
     * @return ParallelDeadlineGroup with this command as the deadline, with n next commands
     */
    fun deadlineWith(vararg parallel: Cmd) = DeadlineGroup(this, *parallel)

    /**
     * Run n commands in parallel with this command, ending when all commands have ended
     * @param parallel commands to run in parallel with this command
     * @return ParallelCommandGroup with this command and n parallel commands
     */
    fun alongWith(vararg parallel: Cmd) = ParallelGroup(this, *parallel)

    /**
     * Run n commands in parallel with this command, ending when any of the commands has ended
     * @param parallel commands to run in parallel with this command
     * @return ParallelRaceGroup with this command and n parallel commands
     */
    fun raceWith(vararg parallel: Cmd) = RaceGroup(this, *parallel)

    /**
     * Name the current command, which shows up in the logger.
     */
    fun withName(commandName: String): Cmd {
        _name = commandName
        return this
    }

    operator fun unaryPlus() = schedule()

    /**
     * Schedule command. Syntax sugar for [KScheduler.schedule]
     */
    fun schedule() {
        KScheduler.schedule(this)
    }

    /**
     * Cancel command. [KScheduler.cancel]
     */
    fun cancel() {
        KScheduler.cancel(this)
    }

    override fun toString() = name
}
