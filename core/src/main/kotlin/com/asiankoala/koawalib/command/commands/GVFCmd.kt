package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.command.group.SequentialGroup
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.path.ProjQuery
import com.asiankoala.koawalib.path.gvf.GVFController
import com.asiankoala.koawalib.subsystem.drive.KMecanumOdoDrive

class GVFCmd(
    private val drive: KMecanumOdoDrive,
    private val controller: GVFController,
    vararg cmds: Pair<Cmd, ProjQuery>,
) : Cmd() {
    private val projCmd: Cmd? = if (cmds.isNotEmpty()) {
        val l = controller.path.length
        SequentialGroup(
            *cmds
                .map {
                    Pair(
                        it.first,
                        controller.path.project(
                            it.second.v,
                            it.second.t?.times(l) ?: (l / 2.0)
                        )
                    )
                }
                .sortedBy { it.second }
                .flatMap {
                    listOf(
                        WaitUntilCmd { controller.s > it.second },
                        it.first
                    )
                }
                .toTypedArray()
        )
    } else null

    override fun initialize() {
        projCmd?.schedule()
    }

    override fun execute() {
        controller.update()
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
