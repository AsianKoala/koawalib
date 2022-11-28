package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.path.gvf.GVFController
import com.asiankoala.koawalib.subsystem.drive.KMecanumOdoDrive

class GVFCmd(
    private val drive: KMecanumOdoDrive,
    private val controller: GVFController,
    vararg cmds: Pair<Cmd, Vector>,
    private val requireCmdsFinished: Boolean
) : Cmd() {
    private val projCmds: List<Pair<Double, Cmd>>
    private var idx = 0

    override fun execute() {
        drive.powers = controller.update(drive.pose)
        if(idx < projCmds.size && controller.s > projCmds[idx].first) {
            + projCmds[idx].second
            idx++
        }
    }

    override fun end() {
        drive.powers = Pose()
    }

    override val isFinished: Boolean
        get() = controller.isFinished
                && (!requireCmdsFinished || (requireCmdsFinished && idx >= projCmds.size))

    init {
        addRequirements(drive)
        val temp = mutableListOf<Pair<Double, Cmd>>()
        cmds.forEach {
            val s = controller.path.project(it.second)
            temp.add(Pair(s, it.first))
        }
        temp.sortBy { it.first }
        projCmds = temp
    }
}
