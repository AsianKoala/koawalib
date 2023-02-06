package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.command.group.SequentialGroup
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.path.ProjQuery
import com.asiankoala.koawalib.path.gvf.GVFController
import com.asiankoala.koawalib.subsystem.drive.KMecanumOdoDrive

class GVFCmd(
    private val drive: KMecanumOdoDrive,
    private val controller: GVFController,
    vararg cmds: ProjQuery,
) : Cmd() {
    private val projCmd: Cmd? = if (cmds.isNotEmpty()) {
        val l = controller.path.length
        SequentialGroup(
            *cmds
                .sortedBy { it.t }
                .flatMap {
                    listOf(
                        WaitUntilCmd { controller.disp / controller.path.length > it.t },
                        it.cmd
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
        Logger.logInfo("finished path")
        drive.powers = Pose()
    }

    override val isFinished: Boolean
        get() = controller.isFinished

    init {
        addRequirements(drive)
    }
}
