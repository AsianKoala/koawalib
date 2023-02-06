package com.asiankoala.koawalib.path.gvf

import com.asiankoala.koawalib.control.controller.PIDFController
import com.asiankoala.koawalib.control.controller.PIDGains
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.math.degrees
import com.asiankoala.koawalib.path.HermitePath
import com.asiankoala.koawalib.subsystem.drive.KMecanumOdoDrive
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.sign

/**
 *  Guided Vector Field follower
 *  @link https://arxiv.org/pdf/1610.04391.pdf
 *  @param path path
 *  @param kN normal path attraction
 *  @param kOmega heading weight
 *  @param kF end param weight
 *  @param kS raw scalar on translational power
 *  @param epsilon allowed absolute and projected error
 *  @param errorMap error map to transform normal displacement error
 *  @property isFinished path finish state
 */
class SimpleGVFController(
    override val path: HermitePath,
    override val drive: KMecanumOdoDrive,
    private val kN: Double,
    private val kF: Double,
    private val kS: Double,
    private val epsilon: Double,
    private val thetaEpsilon: Double,
    private val epsilonToPID: Double,
    transPIDGains: PIDGains,
    thetaPIDGains: PIDGains,
    private val errorMap: (Double) -> Double = { it },
) : GVFController {
    private var headingError = 0.0
    private val xController = PIDFController(transPIDGains)
    private val yController = PIDFController(transPIDGains)
    private val thetaController = PIDFController(thetaPIDGains).apply {
        setInputBounds(-PI, PI)
    }

    override var disp: Double = 0.0
    override val isFinished
        get() = path.length - disp < epsilon &&
            drive.pose.vec.dist(path.end.vec) < epsilon &&
            headingError.absoluteValue < thetaEpsilon

    private fun calcGVF(): Vector {
        Logger.addTelemetryLine("length - s: ${path.length - disp}, dist: ${drive.pose.vec.dist(path.end.vec)}, headingError: ${headingError.absoluteValue}")
        val tangent = path[disp, 1].vec
        val normal = tangent.rotate(PI / 2.0)
        val displacementVec = path[disp].vec - drive.pose.vec
        val error = displacementVec.norm * (displacementVec cross tangent).sign
        return (tangent - normal * kN * errorMap.invoke(error)).unit
    }

    private fun headingControl(): Pair<Double, Double> {
        thetaController.targetPosition = path[disp].heading
        val output = thetaController.update(drive.pose.heading, drive.vel.heading)
        headingError = (path[disp].heading - drive.pose.heading).angleWrap.degrees
        return Pair(output, headingError)
    }

    private fun vectorControl(v: Vector): Vector {
        return v * kS * min(1.0, (path.length - disp) / kF)
    }

    override fun update() {
        disp = path.project(drive.pose.vec, disp)
        val headingResult = headingControl()
        val vectorResult = if (drive.pose.vec.dist(path.end.vec) < epsilonToPID) {
            xController.targetPosition = path.end.vec.x
            yController.targetPosition = path.end.vec.y
            val xOutput = xController.update(drive.pose.x, drive.vel.x)
            val yOutput = yController.update(drive.pose.y, drive.vel.y)
            Vector(xOutput, yOutput)
        } else {
            vectorControl(calcGVF())
        }
        drive.powers = Pose(vectorResult.rotate(PI / 2.0 - drive.pose.heading), headingResult.first)
    }

    init {
        require(kS <= 1.0)
    }
}
