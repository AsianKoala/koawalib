package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.radians
import com.asiankoala.koawalib.math.wrap
import com.asiankoala.koawalib.path.PurePursuitController
import com.asiankoala.koawalib.subsystem.drive.KMecanumOdoDrive
import kotlin.math.absoluteValue

class GoToPointCommand(
    private val drive: KMecanumOdoDrive,
    private val target: Pose,
    private val distTol: Double,
    private val angleTol: Double,
    private val followAngle: Double = 0.0,
    private val  stop: Boolean = false,
    private val maxMoveSpeed: Double = 1.0,
    private val maxTurnSpeed: Double = 1.0,
    private val deccelAngle: Double = 60.0.radians,
    private val isHeadingLocked: Boolean = false,
    private val headingLockAngle: Double = 0.0,
    private val slowDownTurnRadians: Double = 60.0.radians,
    private val lowestSlowDownFromTurnError: Double = 0.4,
    private val shouldTelemetry: Boolean = true
) : CommandBase() {

    override fun execute() {
        val ret = PurePursuitController.goToPosition(
            drive.position,
            target,
            followAngle,
            stop,
            maxMoveSpeed,
            maxTurnSpeed,
            deccelAngle,
            isHeadingLocked,
            headingLockAngle,
            slowDownTurnRadians,
            lowestSlowDownFromTurnError,
            shouldTelemetry
        )

        drive.powers = ret
    }

    override fun end(interrupted: Boolean) {
        drive.powers = Pose()
    }

    override val isFinished: Boolean
        get() = drive.position.dist(target) < distTol && (target.heading - drive.position.heading).wrap.absoluteValue < angleTol

    init {
        addRequirements(drive)
    }
}