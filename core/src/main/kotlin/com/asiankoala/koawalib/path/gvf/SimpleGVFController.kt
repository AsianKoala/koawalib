package com.asiankoala.koawalib.path.gvf

import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.math.degrees
import com.asiankoala.koawalib.path.Path
import com.asiankoala.koawalib.util.Speeds
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
    private val path: Path,
    private val kN: Double,
    private val kOmega: Double,
    private val kF: Double,
    private val kS: Double,
    private val epsilon: Double,
    private val thetaEpsilon: Double,
    private val errorMap: (Double) -> Double = { it },
) : GVFController {
    override var isFinished = false
        private set

    private var pose: Pose = Pose()
    private var s: Double = 0.0

    private fun calcGVF(): Vector {
        val tangent = path[s, 1].vec
        val normal = tangent.rotate(PI / 2.0)
        val displacementVec = path[s].vec - pose.vec
        val error = displacementVec.norm * (displacementVec cross tangent).sign
        Logger.logInfo("s: $s, d: $tangent, r: $displacementVec, e: $error")
        return tangent - normal * kN * errorMap.invoke(error)
    }

    private fun headingControl(): Pair<Double, Double> {
        val error = (path[s].heading - pose.heading).angleWrap.degrees
        val result = error / kOmega
        return Pair(result, error)
    }

    private fun vectorControl(v: Vector): Vector {
        return v * kS * min(1.0, (path.length - s) / kF)
    }

    override fun update(currPose: Pose): Pose {
        pose = currPose
        s = path.project(pose.vec, s)
        val headingResult = headingControl()
        val vectorResult = vectorControl(calcGVF())
        isFinished = path.length - s < epsilon &&
                pose.vec.dist(path.end.vec) < epsilon &&
                headingResult.second.absoluteValue < thetaEpsilon
        return Speeds()
            .apply { setFieldCentric(Pose(vectorResult, headingResult.first)) }
            .getRobotCentric(pose.heading)
    }

    init {
        require(kS <= 1.0)
    }
}
