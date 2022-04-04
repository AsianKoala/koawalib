package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.gamepad.functionality.Stick
import com.asiankoala.koawalib.math.*
import com.asiankoala.koawalib.math.Point
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.subsystem.drive.KMecanumDrive
import com.asiankoala.koawalib.util.Alliance

class MecanumDriveCommand(
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
    private val fieldOriented: Boolean = false,
    private val headingLock: Boolean = false,
    private val heading: () -> Double = { Double.NaN },
    private val headingLockScalar: Double = 90.0.radians
) : CommandBase() {

    override fun execute() {
        val xRaw = leftStick.xSupplier.invoke()
        val yRaw = -leftStick.ySupplier.invoke()
        val rRaw = rightStick.xSupplier.invoke()

        val xScaled = cubicScaling(xCubic, xRaw) * xScalar
        val yScaled = cubicScaling(yCubic, yRaw) * yScalar
        val rScaled = cubicScaling(rCubic, rRaw) * rScalar

        val final = if (fieldOriented) {
            val translationVector = Point(xScaled, yScaled)
            val headingInvoked = heading.invoke()
            val rotatedTranslation = translationVector.rotate(-heading.invoke() + if(alliance == Alliance.RED) 180.0.radians else 0.0)

            val turn = if (headingLock && !headingInvoked.isNaN()) {
                val stickAtan = rightStick.angle
                val deltaAngle = (headingInvoked - stickAtan).angleWrap
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
