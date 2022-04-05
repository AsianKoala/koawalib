package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.path.Path
import com.asiankoala.koawalib.subsystem.drive.KMecanumOdoDrive

class PathCommand(
    private val drive: KMecanumOdoDrive,
    private val path: Path
) : CommandBase() {

    override fun initialize() {
        drive.powers = Pose()
    }

    override fun execute() {
//        drive.powers = path.update(drive.position)
    }

    override fun end(interrupted: Boolean) {
        drive.powers = Pose()
    }

    override val isFinished: Boolean get() = path.isFinished

    init {
        addRequirements(drive)
    }
}
