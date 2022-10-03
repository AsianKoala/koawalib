package com.asiankoala.koawalib.pathing

import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.math.degrees
import com.asiankoala.koawalib.util.Speeds

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
        val desiredHeading = lastTangentVec.angle
        val headingError = (desiredHeading - lastPose.heading).angleWrap.degrees
        val result = kOmega * headingError
        return Pair(result, headingError)
    }

    override fun vectorControl(): Vector {
        val paramTillEnd = path.length - lastS
        var translationalPower = (lastGVFVec / lastGVFVec.norm) * kS
        if (paramTillEnd < kF) translationalPower /= kF

        val endRVector = path.end.vec - lastPose.vec
        isFinished = paramTillEnd < epsilon && endRVector.norm < epsilon
        if (isFinished) return Vector()

        if (translationalPower.norm > 1.0) translationalPower /= translationalPower.norm
        return translationalPower
    }

    override fun update(currPose: Pose, currVel: Speeds): Speeds {
        lastPose = currPose
        lastS = path.project(lastPose.vec, lastS)

        val vectorFieldResult = gvfVecAt(lastPose, lastS)

        lastGVFVec = vectorFieldResult

        val headingResult = headingControl()
        val angularOutput = headingResult.first
        val headingError = headingResult.second

        lastHeadingError = headingError

        val vectorResult = vectorControl()

        val speeds = Speeds()
        speeds.setFieldCentric(Pose(vectorResult, angularOutput))
        return speeds
    }
}
