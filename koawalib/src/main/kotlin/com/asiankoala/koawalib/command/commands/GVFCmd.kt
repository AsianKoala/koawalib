package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.path.gvf.GVFController
import com.asiankoala.koawalib.subsystem.drive.KMecanumOdoDrive
import com.asiankoala.koawalib.util.Speeds

class GVFCmd(
    private val drive: KMecanumOdoDrive,
    private val controller: GVFController
) : Cmd() {
    override fun execute() {
        drive.powers = controller.update(
            drive.pose,
            Speeds().apply { setRobotCentric(drive.vel, drive.pose.heading) }
        ).getRobotCentric(drive.pose.heading)
    }

    override fun end() {
        drive.powers = Pose()
    }

    override val isFinished: Boolean
        get() = controller.isFinished

    init {
        addRequirements(drive)
    }
}
