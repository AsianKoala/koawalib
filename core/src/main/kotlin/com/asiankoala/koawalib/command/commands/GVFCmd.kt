package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.command.group.SequentialGroup
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.path.gvf.GVFController
import com.asiankoala.koawalib.subsystem.drive.KMecanumOdoDrive

class GVFCmd(
    private val drive: KMecanumOdoDrive,
    private val controller: GVFController,
    vararg cmds: Pair<Cmd, Vector>,
) : Cmd() {
    private val projCmd = SequentialGroup(
        *cmds
            .map { Pair(it.first, controller.path.project(it.second)) }
            .sortedBy { it.second }
            .flatMap {
                listOf(
                    WaitUntilCmd { controller.s > it.second },
                    it.first
                )
            }
            .toTypedArray()
    )

    override fun initialize() {
        + projCmd
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
