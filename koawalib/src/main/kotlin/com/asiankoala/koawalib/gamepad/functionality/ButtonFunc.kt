package com.asiankoala.koawalib.gamepad.functionality

import com.asiankoala.koawalib.command.KScheduler
import com.asiankoala.koawalib.command.commands.Cmd
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.util.OpModeState

// todo log button -> cmd somehow
// e.g. in logcat it should look like this
// added watchdog to RumbleGamepad mapped to A.onPress
interface ButtonFunc {
    val isJustPressed: Boolean
    val isJustReleased: Boolean
    val isPressed: Boolean
    val isReleased: Boolean
    val isToggled: Boolean

    /**
     * Schedule command on press
     * @param cmd command to schedule
     */
    fun onPress(cmd: Cmd) {
        schedule(::isJustPressed, cmd)
    }

    /**
     * Schedule command on release
     * @param cmd command to schedule
     */
    fun onRelease(cmd: Cmd) {
        schedule(::isJustReleased, cmd)
    }

    /**
     * Schedule command while pressed
     * @param cmd command to schedule
     */
    fun whilePressed(cmd: Cmd) {
        schedule(::isPressed, cmd)
    }

    /**
     * Schedule command while released
     * @param cmd command to schedule
     */
    fun whileReleased(cmd: Cmd) {
        schedule(::isReleased, cmd)
    }

    /**
     * Schedule command when pressed to cancel on release
     * @param cmd command to schedule
     */
    fun onPressUntilRelease(cmd: Cmd) {
        schedule(::isJustPressed, cmd.cancelIf(::isReleased))
    }

    /**
     * Schedule a command when toggled
     * @param cmd command to schedule
     */
    fun onToggle(cmd: Cmd) {
        schedule(::isToggled, cmd)
    }

    private fun schedule(condition: () -> Boolean, cmd: Cmd) {
        + object : Cmd() {
            override fun execute() {
                if (condition.invoke() && KScheduler.stateReceiver.invoke() == OpModeState.LOOP) {
                    cmd.schedule()
                }
            }

            override val isFinished: Boolean = false
        }
        Logger.logInfo("added watchdog $cmd")
    }
}
