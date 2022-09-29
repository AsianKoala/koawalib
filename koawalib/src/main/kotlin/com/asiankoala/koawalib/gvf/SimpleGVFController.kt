package com.asiankoala.koawalib.gvf

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
 */
class SimpleGVFController(
    path: Pathing.Path,
    kN: Double,
    kOmega: Double,
    private val kF: Double,
    private val kS: Double,
    epsilon: Double,
    errorMap: (Double) -> Double = { it },
) : GVFController(path, kN, kOmega, epsilon, errorMap) {

    override fun headingControl(): Pair<Double, Double> {
        // note to neil: just leave this gvf controller untouched since it works
        // need to find a better solution to the heading problem though..
        val desiredHeading = lastTangentVec.angle
        val headingError = (desiredHeading - lastPose.heading).angleWrap.degrees
        val result = kOmega * headingError
        return Pair(result, headingError)
    }

    override fun vectorControl(): Vector {
        val paramTillEnd = path.length() - lastS
        var translationalPower = (lastGVFVec / lastGVFVec.norm) * kS
        if (paramTillEnd < kF) translationalPower /= kF

        val endRVector = path.end().vec - lastPose.vec
        isFinished = paramTillEnd < epsilon && endRVector.norm < epsilon
        if (isFinished) return Vector()

        if (translationalPower.norm > 1.0) translationalPower /= translationalPower.norm
        return translationalPower
    }

    override fun update(currPose: Pose, currVel: Speeds): Speeds {
        lastPose = currPose
        lastS = if (lastS.isNaN()) {
            path.fastProject(lastPose.vec, path.length() * 0.1)
        } else {
            path.fastProject(lastPose.vec, lastS)
        }

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
