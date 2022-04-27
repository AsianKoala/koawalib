package com.asiankoala.koawalib.gamepad

import com.asiankoala.koawalib.gamepad.functionality.Button
import com.asiankoala.koawalib.math.d
import com.asiankoala.koawalib.util.Periodic
import com.qualcomm.robotcore.hardware.Gamepad

@Suppress("unused", "WeakerAccess")
class KGamepad(private val gamepad: Gamepad) : Periodic {
    val a = KButton { gamepad.a }
    val b = KButton { gamepad.b }
    val x = KButton { gamepad.x }
    val y = KButton { gamepad.y }

    val start = KButton { gamepad.start }
    val back = KButton { gamepad.back }

    val leftBumper = KButton { gamepad.left_bumper }
    val rightBumper = KButton { gamepad.right_bumper }

    val leftTrigger = KTrigger { gamepad.left_trigger.d }
    val rightTrigger = KTrigger { gamepad.right_trigger.d }

    val leftStickButton = KButton { gamepad.left_stick_button }
    val rightStickButton = KButton { gamepad.right_stick_button }
    val leftStickX = KAxis { gamepad.left_stick_x.d }
    val leftStickY = KAxis { gamepad.left_stick_y.d }
    val rightStickX = KAxis { gamepad.right_stick_x.d }
    val rightStickY = KAxis { gamepad.right_stick_y.d }

    val leftStick = KStick(leftStickX, leftStickY, leftStickButton)
    val rightStick = KStick(rightStickX, rightStickY, rightStickButton)

    val dpadUp = KButton { gamepad.dpad_up }
    val dpadDown = KButton { gamepad.dpad_down }
    val dpadLeft = KButton { gamepad.dpad_left }
    val dpadRight = KButton { gamepad.dpad_right }

    private val periodics: Array<Periodic> = arrayOf(
        a, b, x, y, start, back, leftBumper, rightBumper,
        leftTrigger, rightTrigger, leftStick, rightStick, dpadUp, dpadDown, dpadLeft, dpadRight
    )

    override fun periodic() {
        periodics.forEach(Periodic::periodic)
    }
}
