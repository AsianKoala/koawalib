package com.asiankoala.koawalib.pathing

import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.math.degrees
import com.asiankoala.koawalib.util.Speeds
import kotlin.math.min

/**
 *  Guided Vector Field follower
 *  Uses roadrunner path generation internally cause im lazy
 *  @link https://arxiv.org/pdf/1610.04391.pdf
 *  @param path roadrunner path
 *  @param kN normal path attraction
 *  @param kOmega heading weight
 *  @param kF end param weight
 *  @param epsilon allowed absolute and projected error
 *  @param errorMap error map to transform normal displacement error
 *  @property isFinished path finish state
 *  possible improvements i can make:
 *  kLookAhead: instead of relying on tangent angle, just take (vec - get(proj + kLookahead).angle
 *  this also should incorporate some sort of slowdown towards end of path (reduce jerkiness from heading change)
 *  kTheta: scale translational power down by a weight proportional to heading error
 *  kTheta would literally mean the value at which going any higher (in error) would result in slowdown
 *  something like this: scaled = translational * max(1.0, kTheta / headingError.absolute)
 *  min cause we don't want to scale it upwards
 */
class SimpleGVFController(
    path: Path,
    kN: Double,
    kOmega: Double,
    private val kF: Double,
    private val kS: Double,
    epsilon: Double,
    errorMap: (Double) -> Double = { it },
) : GVFController(path, kN, kOmega, epsilon, errorMap) {
    override fun headingControl(): Pair<Double, Double> {
        val headingError = (tangent.angle - pose.heading).angleWrap.degrees
        val result = kOmega * headingError
        return Pair(result, headingError)
    }

    override fun vectorControl(): Vector {
        return gvfVec * kS * min(1.0, (path.length - s) / kF)
    }

    override fun process(currPose: Pose, currVel: Speeds): Speeds {
        super.update(currPose, currVel)
        val speeds = Speeds()
        speeds.setFieldCentric(Pose(vectorResult, headingResult.first))
        return speeds
    }

    init {
        require(kS <= 1.0)
    }
}
