package com.asiankoala.koawalib.gamepad.functionality

import com.asiankoala.koawalib.command.CommandScheduler
import com.asiankoala.koawalib.command.commands.Command
import com.asiankoala.koawalib.gamepad.ButtonBase
import com.asiankoala.koawalib.util.KBoolean

interface Input<T : ButtonBase> : KBoolean {
    fun whenPressed(command: Command) {
        schedule(instance()::isJustPressed, command)
    }

    fun whenReleased(command: Command) {
        schedule(instance()::isJustReleased, command)
    }

    fun whilePressed(command: Command) {
        schedule(instance()::isPressed, command.cancelUpon(instance()::isReleased))
    }

    fun whileReleased(command: Command) {
        schedule(instance()::isReleased, command.cancelUpon(instance()::isPressed))
    }

    fun whilePressedOnce(command: Command) {
        schedule(instance()::isJustPressed, command.cancelUpon(instance()::isReleased))
    }

    fun whilePressedContinuous(command: Command) {
        schedule(instance()::isPressed, command)
    }

    fun whileReleasedOnce(command: Command) {
        schedule(instance()::isJustReleased, command.cancelUpon(instance()::isPressed))
    }

    fun whenToggled(command: Command) {
        schedule(instance()::isJustToggled, command)
    }

    fun whenInverseToggled(command: Command) {
        schedule(instance()::isJustUntoggled, command)
    }

    fun whileToggled(command: Command) {
        schedule(instance()::isToggled, command.cancelUpon(instance()::isUntoggled))
    }

    fun whileInverseToggled(command: Command) {
        schedule(instance()::isUntoggled, command.cancelUpon(instance()::isToggled))
    }

    fun instance(): T

    fun schedule(condition: () -> Boolean, command: Command) {
        CommandScheduler.scheduleWatchdog(condition, command)
        instance()
    }

    fun schedule(command: Command) {
        schedule({ true }, command)
    }
}
