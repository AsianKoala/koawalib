package com.asiankoala.koawalib.command.commands

import com.acmerobotics.roadrunner.path.Path
import com.asiankoala.koawalib.gvf.SimpleGVFController
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.subsystem.drive.KMecanumOdoDrive
import com.asiankoala.koawalib.util.Speeds

class GVFCmd(
    private val drive: KMecanumOdoDrive,
    path: Path,
    kN: Double,
    kOmega: Double,
    kF: Double,
    epsilon: Double,
    errorMap: (Double) -> Double = { it }
) : Cmd() {
    private val controller = SimpleGVFController(path, kN, kOmega, kF, epsilon, errorMap)

    override fun execute() {
        drive.powers = controller.update(drive.pose,
            Speeds().apply { setRobotCentric(drive.vel, drive.pose.heading) })
            .getRobotCentric(drive.pose.heading)
    }

    override fun end() {
        drive.powers = Pose()
    }

    override val isFinished: Boolean
        get() = controller.isFinished
}
