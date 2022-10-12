package com.asiankoala.koawalib.path.gvf

import com.asiankoala.koawalib.math.*
import com.asiankoala.koawalib.path.Path
import com.asiankoala.koawalib.util.Speeds
import kotlin.math.absoluteValue
import kotlin.math.min

/**
 *  Guided Vector Field follower
 *  Uses roadrunner path generation internally cause im lazy
 *  @link https://arxiv.org/pdf/1610.04391.pdf
 *  @param path roadrunner path
 *  @param kN normal path attraction
 *  @param kOmega heading weight
 *  @param kF end param weight
 *  @param kS raw scalar on translational power
 *  @param kLookahead turning lookahead
 *  @param kTheta translational heading error scalar
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
class ExtGVFController(
    path: Path,
    kN: Double,
    kOmega: Double,
    private val kF: Double,
    private val kS: Double,
    private val kLookahead: Double,
    private val kTheta: Double,
    epsilon: Double,
    errorMap: (Double) -> Double = { it }
) : GVFController(path, kN, kOmega, epsilon, errorMap) {
    override fun headingControl(vel: Speeds): Pair<Double, Double> {
        val target = path[clamp(s + kLookahead, 0.0, path.length)].heading
        val error = (target - pose.heading).angleWrap.degrees
        val result = kOmega * error
        return Pair(result, error)
    }

    override fun vectorControl(vel: Speeds): Vector {
        val endScalar = min(1.0, (path.length - s) / kF)
        val headingErrorScalar = min(1.0, kTheta / headingResult.second.absoluteValue)
        return gvfVec * kS * endScalar * headingErrorScalar
    }

    init {
        require(kS <= 1.0)
    }
}
