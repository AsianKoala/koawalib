package com.asiankoala.koawalib.gamepad

import com.asiankoala.koawalib.command.CommandScheduler
import com.asiankoala.koawalib.command.commands.Command
import com.asiankoala.koawalib.gamepad.functionality.Button
import com.asiankoala.koawalib.util.KBoolean

@Suppress("unused")
class KButton(private val buttonState: () -> Boolean) : Button(), KBoolean {
    fun onPress(command: Command) {
        schedule(::isJustPressed, command)
    }

    fun onRelease(command: Command) {
        schedule(::isJustReleased, command)
    }

    fun whilePressed(command: Command) {
        schedule(::isPressed, command)
    }

    fun whileReleased(command: Command) {
        schedule(::isReleased, command)
    }

    fun onPressUntilRelease(command: Command) {
        schedule(::isJustPressed, command.cancelUpon(::isReleased))
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

    override fun invokeBoolean(): Boolean {
        return buttonState.invoke()
    }
}
