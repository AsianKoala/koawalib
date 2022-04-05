package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.radians
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.path.PurePursuitController
import com.asiankoala.koawalib.subsystem.drive.KMecanumOdoDrive
import kotlin.math.absoluteValue

class GoToPointCommand(
    private val drive: KMecanumOdoDrive,
    private val target: Pose,
    private val distTol: Double,
    private val angleTol: Double,
    private val stop: Boolean = false,
    private val maxMoveSpeed: Double = 1.0,
    private val maxTurnSpeed: Double = 1.0,
    private val deccelAngle: Double = 60.0.radians,
    private val slowDownTurnRadians: Double = 60.0.radians,
    private val lowestSlowDownFromTurnError: Double = 0.4,
) : CommandBase() {

    override fun execute() {
        val ret = PurePursuitController.goToPosition(
            drive.position,
            target,
            stop,
            maxMoveSpeed,
            maxTurnSpeed,
            deccelAngle,
            target.heading,
            slowDownTurnRadians,
            lowestSlowDownFromTurnError,
        )

        drive.powers = ret
    }

    override fun end(interrupted: Boolean) {
        drive.powers = Pose()
    }

    override val isFinished: Boolean
        get() = drive.position.dist(target) < distTol && (target.heading - drive.position.heading).angleWrap.absoluteValue < angleTol

    init {
        addRequirements(drive)
    }
}