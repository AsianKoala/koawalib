package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.path.purepursuit.PurePursuitPath
import com.asiankoala.koawalib.subsystem.drive.KMecanumOdoDrive

class PathCommand(
    private val drive: KMecanumOdoDrive,
    private val purePursuitPath: PurePursuitPath,
    private val tol: Double
) : CommandBase() {

    override fun initialize() {
        drive.powers = Pose()
    }

    override fun execute() {
        drive.powers = purePursuitPath.update(drive.pose, tol).first
    }

    override fun end() {
        drive.powers = Pose()
    }

    override val isFinished: Boolean get() = purePursuitPath.isFinished

    init {
        addRequirements(drive)
    }
}
