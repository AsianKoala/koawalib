package com.asiankoala.koawalib.roadrunner.drive

import com.asiankoala.koawalib.command.commands.CommandBase
import com.asiankoala.koawalib.roadrunner.trajectorysequence.TrajectorySequence

class TrajectoryRRCommand(
    private val drive: KMecanumDriveRR,
    private val trajectorySequence: TrajectorySequence
) : CommandBase() {

    override fun initialize() {
        drive.followTrajectorySequenceAsync(trajectorySequence)
    }

    override fun execute() {
        drive.update()
    }

    override val isFinished: Boolean
        get() = !drive.isBusy()

    init {
        addRequirements(drive)
    }
}