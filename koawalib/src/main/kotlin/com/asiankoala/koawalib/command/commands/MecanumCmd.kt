package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.gamepad.functionality.Stick
import com.asiankoala.koawalib.math.*
import com.asiankoala.koawalib.subsystem.drive.KMecanumDrive
import com.asiankoala.koawalib.util.Alliance

/**
 * TeleOp drive control command
 * vector drive power is calculated with the function:
 * f(x) = scalar * ((1-k)x+kx^3)
 * e.g. xPower = xScalar * ((1-xCubic) * leftStick.x + xCubic * leftStick.x^3)
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
    private val leftStick: Stick,
    private val rightStick: Stick,
    private val xCubic: Double = 1.0,
    private val yCubic: Double = 1.0,
    private val rCubic: Double = 1.0,
    private val xScalar: Double = 1.0,
    private val yScalar: Double = 1.0,
    private val rScalar: Double = 1.0,
    private val alliance: Alliance = Alliance.BLUE,
    private val isTranslationFieldCentric: Boolean = false,
    private val isHeadingFieldCentric: Boolean = false,
    private val heading: () -> Double = { Double.NaN },
    private val fieldCentricHeadingScalar: Double = 90.0.radians
) : Cmd() {

    /**
     * Sets scaled power to mecanum drive
     */
    override fun execute() {
        val xRaw = leftStick.xSupplier.invoke()
        val yRaw = -leftStick.ySupplier.invoke()
        val rRaw = -rightStick.xSupplier.invoke()

        val xScaled = cubicScaling(xCubic, xRaw) * xScalar
        val yScaled = cubicScaling(yCubic, yRaw) * yScalar
        val rScaled = cubicScaling(rCubic, rRaw) * rScalar

        val final = if (isTranslationFieldCentric) {
            val translationVector = Vector(xScaled, yScaled)
            val headingInvoked = heading.invoke()
            val rotatedTranslation = translationVector.rotate(-heading.invoke() + if (alliance == Alliance.RED) 180.0.radians else 0.0)

            val turn = if (isHeadingFieldCentric && !headingInvoked.isNaN()) {
                val stickAtan = rightStick.angle
                val deltaAngle = (headingInvoked - stickAtan).angleWrap
                val rLockScaled = deltaAngle / fieldCentricHeadingScalar

                rLockScaled
            } else {
                rScaled
            }

            Pose(rotatedTranslation, turn)
        } else {
            Pose(xScaled, yScaled, rScaled)
        }

        drive.powers = final
    }

    override val isFinished: Boolean
        get() = false

    init {
        addRequirements(drive)
    }
}
