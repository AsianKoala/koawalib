package com.asiankoala.koawalib.subsystem.drive

import com.asiankoala.koawalib.command.commands.CommandBase
import com.asiankoala.koawalib.gamepad.functionality.Stick
import com.asiankoala.koawalib.math.*
import com.asiankoala.koawalib.math.Point
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.util.Alliance

class MecanumDriveCommand(
    private val drive: KMecanumDrive,
    private val leftStick: Stick,
    private val rightStick: Stick,
    private val xCubic: Double = 1.0,
    private val yCubic: Double = 1.0,
    private val rCubic: Double = 1.0,
    private val alliance: Alliance = Alliance.BLUE,
    private val fieldOriented: Boolean = false,
    private val headingLock: Boolean = false,
    private val heading: () -> Double = { Double.NaN },
    private val headingLockScalar: Double = 90.0
) : CommandBase() {

    override fun execute() {
        val xRaw = leftStick.xSupplier.invoke()
        val yRaw = -leftStick.ySupplier.invoke()
        val rRaw = rightStick.xSupplier.invoke()

        val xScaled = cubicScaling(xCubic, xRaw)
        val yScaled = cubicScaling(yCubic, yRaw)
        val rScaled = cubicScaling(rCubic, rRaw)

        val final = if (fieldOriented) {
            val translationVector = Point(xScaled, yScaled)
            val headingInvoked = heading.invoke()
            val rotatedTranslation = translationVector.rotate(heading.invoke() + alliance.decide(90, -90).d.radians)

            val turn = if (headingLock && !headingInvoked.isNaN()) {
                val stickAtan = rightStick.angle
                val deltaAngle = (headingInvoked - stickAtan).wrap
                val rLockScaled = deltaAngle / headingLockScalar

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

    class Test {
        fun t() {
        }
    }
}
