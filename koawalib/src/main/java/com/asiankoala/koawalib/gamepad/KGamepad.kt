package com.asiankoala.koawalib.gamepad

import com.asiankoala.koawalib.command.CommandScheduler
import com.asiankoala.koawalib.command.commands.Command
import com.asiankoala.koawalib.gamepad.functionality.Stick
import com.asiankoala.koawalib.math.MathUtil.d
import com.asiankoala.koawalib.util.Periodic
import com.qualcomm.robotcore.hardware.Gamepad

class KGamepad(private val gamepad: Gamepad) : Periodic {
    val a = GamepadButton { gamepad.a }
    val b = GamepadButton { gamepad.b }
    val x = GamepadButton { gamepad.x }
    val y = GamepadButton { gamepad.y }

    val start = GamepadButton { gamepad.start }
    val back = GamepadButton { gamepad.back }

    val leftBumper = GamepadButton { gamepad.left_bumper }
    val rightBumper = GamepadButton { gamepad.right_bumper }

    val leftStickButton = GamepadButton { gamepad.left_stick_button }
    val rightStickButton = GamepadButton { gamepad.right_stick_button }

    val leftTrigger = GamepadAxis { gamepad.left_trigger.d }
    val rightTrigger = GamepadAxis { gamepad.right_trigger.d }
    val leftStickX = GamepadAxis { gamepad.left_stick_x.d }
    val leftStickY = GamepadAxis { gamepad.left_stick_y.d }
    val rightStickX = GamepadAxis { gamepad.right_stick_x.d }
    val rightStickY = GamepadAxis { gamepad.right_stick_y.d }

    val leftStick = GamepadStick(leftStickX, leftStickY, leftStickButton)
    val rightStick = GamepadStick(rightStickX, rightStickY, rightStickButton)

    val dpadUp = GamepadButton { gamepad.dpad_up }
    val dpadDown = GamepadButton { gamepad.dpad_down }
    val dpadLeft = GamepadButton { gamepad.dpad_left }
    val dpadRight = GamepadButton { gamepad.dpad_right }

    private val periodics: Array<Periodic> = arrayOf(
        a, b, x, y, start, back, leftBumper, rightBumper,
        leftTrigger, rightTrigger, leftStick, rightStick, dpadUp, dpadDown, dpadLeft, dpadRight
    )

    fun scheduleStick(s: Stick, f: (Double, Double) -> Command): KGamepad {
        CommandScheduler.scheduleWatchdog({ true }, f.invoke(s.xAxis, s.yAxis))
        return this
    }

    override fun periodic() {
        periodics.forEach(Periodic::periodic)
    }
}
