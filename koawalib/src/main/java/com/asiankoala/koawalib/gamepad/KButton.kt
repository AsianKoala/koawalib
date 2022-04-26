package com.asiankoala.koawalib.gamepad

import com.asiankoala.koawalib.command.KScheduler
import com.asiankoala.koawalib.command.commands.Cmd
import com.asiankoala.koawalib.gamepad.functionality.Button
import com.asiankoala.koawalib.util.KBoolean

@Suppress("unused")
open class KButton(private val buttonState: () -> Boolean) : Button(), KBoolean {
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

    private fun schedule(condition: () -> Boolean, cmd: Cmd) {
        KScheduler.scheduleWatchdog(condition, cmd)
    }

    override fun invokeBoolean(): Boolean {
        return buttonState.invoke()
    }
}
