package com.asiankoala.koawalib.gamepad

import com.asiankoala.koawalib.command.CommandScheduler
import com.asiankoala.koawalib.command.commands.Command
import com.asiankoala.koawalib.gamepad.functionality.Button
import com.asiankoala.koawalib.util.KBoolean

@Suppress("unused")
class KButton(private val buttonState: () -> Boolean) : Button(), KBoolean {
    /**
     * Schedule command on press
     * @param command command to schedule
     */
    fun onPress(command: Command) {
        schedule(::isJustPressed, command)
    }

    /**
     * Schedule command on release
     * @param command command to schedule
     */
    fun onRelease(command: Command) {
        schedule(::isJustReleased, command)
    }
    /**
     * Schedule command while pressed
     * @param command command to schedule
     */

    fun whilePressed(command: Command) {
        schedule(::isPressed, command)
    }

    /**
     * Schedule command while released
     * @param command command to schedule
     */

    fun whileReleased(command: Command) {
        schedule(::isReleased, command)
    }

    /**
     * Schedule command when pressed to cancel on release
     * @param command command to schedule
     */

    fun onPressUntilRelease(command: Command) {
        schedule(::isJustPressed, command.cancelIf(::isReleased))
    }

    private fun schedule(condition: () -> Boolean, command: Command) {
        CommandScheduler.scheduleWatchdog(condition, command)
    }

    override fun invokeBoolean(): Boolean {
        return buttonState.invoke()
    }
}
