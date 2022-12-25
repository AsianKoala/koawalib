package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.gamepad.KStick
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.math.radians
import com.asiankoala.koawalib.subsystem.drive.KMecanumDrive
import com.asiankoala.koawalib.util.Alliance
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
 * @param xCubic x power k constant in scaling function
 * @param yCubic y power k constant in scaling function
 * @param rCubic r power k constant in scaling function
 * @param xScalar x scalar in scaling function
 * @param yScalar y scalar in scaling function
 * @param rScalar r scalar in scaling function
 * @param alliance robot's alliance for match
 * @param isTranslationFieldCentric translation field centric
 * @param isHeadingFieldCentric heading field centric
 * @param heading heading supplier
 * @param fieldCentricHeadingScalar angle to start deccel for field centric heading
 */
class MecanumCmd(
    private val drive: KMecanumDrive,
    private val leftStick: KStick,
    private val rightStick: KStick,
    private val xScalar: Double = 1.0,
    private val yScalar: Double = 1.0,
    private val rScalar: Double = 1.0,
    private val xCubic: Double = 1.0,
    private val yCubic: Double = 1.0,
    private val rCubic: Double = 1.0,
    private val alliance: Alliance = Alliance.BLUE,
    private val isTranslationFieldCentric: Boolean = false,
    private val isHeadingFieldCentric: Boolean = false,
    private val heading: () -> Double = { Double.NaN },
    private val fieldCentricHeadingScalar: Double = 90.0.radians,
) : Cmd() {
    private fun joystickFunction(s: Double, k: Double, x: Double): Double {
        return s * x * (k * abs(x).pow(3) - k + 1)
    }

    private fun processPowers(): Pose {
        val xRaw = leftStick.xSupplier.invoke()
        val yRaw = -leftStick.ySupplier.invoke()
        val rRaw = -rightStick.xSupplier.invoke()

        val xOutput = joystickFunction(xScalar, xCubic, xRaw)
        val yOutput = joystickFunction(yScalar, yCubic, yRaw)
        val rOutput = joystickFunction(rScalar, rCubic, rRaw)

        return if (isTranslationFieldCentric) {
            val translationVector = Vector(xOutput, yOutput)

            val headingInvoked = heading.invoke()
            val rotatedTranslation = translationVector.rotate(
                -heading.invoke() +
                    if (alliance == Alliance.RED) 180.0.radians else 0.0
            )

            val turn = if (isHeadingFieldCentric && !headingInvoked.isNaN()) {
                val stickAtan = rightStick.angle
                val deltaAngle = (headingInvoked - stickAtan).angleWrap
                val rLockScaled = deltaAngle / fieldCentricHeadingScalar
                rLockScaled
            } else {
                rOutput
            }

            Pose(rotatedTranslation, turn)
        } else {
            Pose(xOutput, yOutput, rOutput)
        }
    }

    override fun execute() {
        drive.powers = processPowers()
    }

    override val isFinished: Boolean
        get() = false

    init {
        addRequirements(drive)
    }
}
