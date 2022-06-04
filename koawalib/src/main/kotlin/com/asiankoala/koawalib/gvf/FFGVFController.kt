package com.asiankoala.koawalib.gvf

import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.path.Path
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.math.degrees
import com.asiankoala.koawalib.util.Speeds
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.max

/**
 *  Guided Vector Field follower
 *  Uses roadrunner path generation internally
 *  @link https://arxiv.org/pdf/1610.04391.pdf
 *  @param path roadrunner path
 *  @param kN normal path attraction
 *  @param kOmega heading weight
 *  @param kF end displacement weight
 *  @param epsilon allowed absolute and projected error
 *  @param errorMap error map to transform normal displacement error
 *  @property isFinished path finish state
 */
class FFGVFController(
    path: Path,
    kN: Double,
    kOmega: Double,
    private val kTheta: Double,
    private val kF: Double,
    private val kLookahead: Double? = null,
    epsilon: Double,
    errorMap: (Double) -> Double = { it },
) : GVFController(path, kN, kOmega, epsilon, errorMap) {

    override fun headingControl(): Pair<Double, Double> {
        var desiredHeading = lastGVFVec.angle()

        if (kLookahead != null) {
            val lookaheadS = lastS + kLookahead
            desiredHeading = (path[lookaheadS].vec() - path[lastS].vec()).angle()
        }
        val headingError = (desiredHeading - lastPose.heading).angleWrap
        return Pair(kOmega * headingError, headingError)
    }

    override fun vectorControl(): Vector2d {
        val projectedDisplacement = (lastS - path.length()).absoluteValue
        var translationalPower = lastGVFVec * (projectedDisplacement / kF)

        val absoluteDisplacement = path.end().vec() - lastPose.vec()
        isFinished = projectedDisplacement < epsilon && absoluteDisplacement.norm() < epsilon
        val absoluteVector = absoluteDisplacement * (projectedDisplacement / kF)

        if (isFinished) translationalPower = absoluteVector
        if (translationalPower.norm() > 1.0) translationalPower /= translationalPower.norm()

        val thetaWeight = lastHeadingError.degrees.absoluteValue / kTheta
        translationalPower /= max(1.0, thetaWeight)
        return translationalPower
    }

    override fun update(currPose: Pose, currVel: Speeds): Speeds {
        lastPose = currPose.toPose2d()
        lastS = if (lastS.isNaN()) {
            path.project(lastPose.vec())
        } else {
            path.fastProject(lastPose.vec(), lastS)
        }

        val vectorFieldResult = gvfVecAt(lastPose, lastS)

        lastGVFVec = vectorFieldResult

        val headingResult = headingControl()
        val angularOutput = headingResult.first
        val headingError = headingResult.second

        lastHeadingError = headingError

        val vectorResult = vectorControl()

        val rotated = vectorResult.rotated(PI / 2.0 - lastPose.heading).toVec()
        val speeds = Speeds()
        speeds.setFieldCentric(Pose(rotated, angularOutput))
        return speeds
    }
}
