package com.asiankoala.koawalib.gamepad.functionality

import com.asiankoala.koawalib.command.CommandScheduler
import com.asiankoala.koawalib.command.commands.Command

abstract class CommandButton : ButtonBase() {
    fun whenPressed(command: Command) {
        schedule(this::isJustPressed, command)
    }

    fun whenReleased(command: Command) {
        schedule(::isJustReleased, command)
    }

    fun whilePressed(command: Command) {
        schedule(::isPressed, command.cancelUpon(::isReleased))
    }

    fun whileReleased(command: Command) {
        schedule(::isReleased, command.cancelUpon(::isPressed))
    }

    fun whilePressedOnce(command: Command) {
        schedule(::isJustPressed, command.cancelUpon(::isReleased))
    }

    fun whilePressedContinuous(command: Command) {
        schedule(::isPressed, command)
    }

    fun whileReleasedOnce(command: Command) {
        schedule(::isJustReleased, command.cancelUpon(::isPressed))
    }

    fun whenToggled(command: Command) {
        schedule(::isJustToggled, command)
    }

    fun whenInverseToggled(command: Command) {
        schedule(::isJustUntoggled, command)
    }

    fun whileToggled(command: Command) {
        schedule(::isToggled, command.cancelUpon(::isUntoggled))
    }

    fun whileInverseToggled(command: Command) {
        schedule(::isUntoggled, command.cancelUpon(::isToggled))
    }

    fun schedule(condition: () -> Boolean, command: Command) {
        CommandScheduler.scheduleWatchdog(condition, command)
    }

    fun schedule(command: Command) {
        schedule({ true }, command)
    }
}
