package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.roadrunner.drive.KMecanumDriveRR
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