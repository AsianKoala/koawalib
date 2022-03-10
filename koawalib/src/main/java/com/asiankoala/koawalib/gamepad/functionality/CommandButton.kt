package com.asiankoala.koawalib.gamepad.functionality

import com.asiankoala.koawalib.command.CommandScheduler
import com.asiankoala.koawalib.command.commands.Command

@Suppress("unused")
abstract class CommandButton : ButtonFunc() {
    fun onPress(command: Command) {
        schedule(::isJustPressed, command)
    }

    fun onRelease(command: Command) {
        schedule(::isJustReleased, command)
    }

    fun whenPressed(command: Command) {
        schedule(::isPressed, command)
    }

    fun whenReleased(command: Command) {
        schedule(::isReleased, command)
    }

    fun onToggle(command: Command) {
        schedule(::isJustToggled, command)
    }

    fun onUnToggle(command: Command) {
        schedule(::isJustUntoggled, command)
    }

    fun whenToggled(command: Command) {
        schedule(::isToggled, command.cancelUpon(::isUntoggled))
    }

    fun whenUntoggled(command: Command) {
        schedule(::isUntoggled, command.cancelUpon(::isToggled))
    }

    fun schedule(condition: () -> Boolean, command: Command) {
        CommandScheduler.scheduleWatchdog(condition, command)
    }

    fun schedule(command: Command) {
        schedule({ true }, command)
    }
}
