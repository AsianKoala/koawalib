package com.asiankoala.koawalib.path.gvf

import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.math.degrees
import com.asiankoala.koawalib.path.Path
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
    override val path: Path,
    override val drive: KMecanumOdoDrive,
    private val kN: Double,
    private val kOmega: Double,
    private val kF: Double,
    private val kS: Double,
    private val epsilon: Double,
    private val thetaEpsilon: Double,
    private val errorMap: (Double) -> Double = { it },
) : GVFController {
    private var pose: Pose = Pose()
    private var headingError = 0.0

    override var s: Double = 0.0
    override val isFinished
        get() = path.length - s < epsilon &&
            pose.vec.dist(path.end.vec) < epsilon &&
            headingError.absoluteValue < thetaEpsilon

    private fun calcGVF(): Vector {
        val tangent = path[s, 1].vec
        val normal = tangent.rotate(PI / 2.0)
        val displacementVec = path[s].vec - pose.vec
        val error = displacementVec.norm * (displacementVec cross tangent).sign
        return (tangent - normal * kN * errorMap.invoke(error)).unit
    }

    private fun headingControl(): Pair<Double, Double> {
        headingError = (path[s].heading - pose.heading).angleWrap.degrees
        val result = headingError / kOmega
        return Pair(result, headingError)
    }

    private fun vectorControl(v: Vector): Vector {
        return v * kS * min(1.0, (path.length - s) / kF)
    }

    override fun update() {
        pose = drive.pose
        s = path.project(pose.vec, s)
        val headingResult = headingControl()
        val vectorResult = vectorControl(calcGVF())
        drive.powers = Pose(vectorResult.rotate(-pose.heading), headingResult.first)
    }

    init {
        require(kS <= 1.0)
    }
}
