package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.path.gvf.GVFController
import com.asiankoala.koawalib.subsystem.drive.KMecanumOdoDrive

class GVFCmd(
    private val drive: KMecanumOdoDrive,
    private val controller: GVFController,
    private vararg val cmds: Pair<Cmd, Vector>
) : Cmd() {
    override fun initialize() {
        // TODO: better gvf cmd integration
//        for (cmd in cmds) {
//            val s = controller.path.project(cmd.second)
//            + WaitUntilCmd { controller.path.project(drive.pose.vec) > s }
//                .andThen(cmd.first)
//        }
    }

    override fun execute() {
        drive.powers = controller.update(drive.pose)
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
