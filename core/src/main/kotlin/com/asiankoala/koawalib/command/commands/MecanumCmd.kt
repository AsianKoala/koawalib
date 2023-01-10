package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.gamepad.KStick
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.subsystem.drive.KMecanumDrive
import kotlin.math.abs
import kotlin.math.pow

/**
 * TeleOp drive control command
 * vector drive power is calculated with the function:
 * f(x) = max(0, s * x * (kx^3 - k + 1)) * sgn(x)
 * e.g. xPower = max(0, xScalar * leftStick.x * (xCubic * leftStick.x ^ 3 - xCubic + 1)
 * see the desmos graph for an understanding of it
 * @see <a href="https://www.desmos.com/calculator/kpc9dcrlrc">https://www.desmos.com/calculator/kpc9dcrlrc</a>
 * If not using field centric drive, leave everything after rScalar as default
 *
 * @param drive KMecanumDrive reference
 * @param leftStick left gamepad joystick
 * @param rightStick right gamepad joystick
 */
class MecanumCmd @JvmOverloads constructor(
    private val drive: KMecanumDrive,
    private val leftStick: KStick,
    private val rightStick: KStick,
    private val scalars: Pose = Pose(1.0, 1.0, 1.0),
    private val cubics: Pose = Pose(1.0, 1.0, 1.0),
    private val constants: Pose = Pose(1.0, 1.0, 1.0),
) : Cmd() {
    private fun joystickFunction(s: Double, k: Double, c: Double, x: Double): Double {
        return s * x * (k * abs(x).pow(3) - k + 1)
    }

    private fun processPowers() = Pose(
        joystickFunction(scalars.x, cubics.x, constants.x, leftStick.xAxis),
        joystickFunction(scalars.y, cubics.y, constants.y, -leftStick.yAxis),
        joystickFunction(scalars.heading, cubics.heading, constants.heading, -rightStick.xAxis),
    )

    override fun execute() {
        drive.powers = processPowers()
    }

    override val isFinished = false

    init {
        addRequirements(drive)
    }
}
