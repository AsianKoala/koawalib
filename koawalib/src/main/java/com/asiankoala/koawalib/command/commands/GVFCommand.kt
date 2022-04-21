package com.asiankoala.koawalib.command.commands

import com.acmerobotics.roadrunner.path.Path
import com.asiankoala.koawalib.gvf.GVFController
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.subsystem.drive.KMecanumOdoDrive

class GVFCommand(
    private val drive: KMecanumOdoDrive,
    path: Path, 
    kN: Double, 
    kOmega: Double, 
    kTheta: Double, 
    kF: Double,
    epsilon: Double,
    errorMap: (Double) -> Double = { it }
) : CommandBase() {
    private val controller = GVFController(path, kN, kOmega, kTheta, kF, epsilon, errorMap)

    override fun execute() {
        drive.powers = controller.update(drive.pose).second
    }

    override fun end() {
        drive.powers = Pose()
    }

    override val isFinished: Boolean
        get() = controller.isFinished
}