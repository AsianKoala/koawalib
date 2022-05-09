package com.asiankoala.koawalib.gvf

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.path.Path
import com.asiankoala.koawalib.gvf.GVFUtil.toVec
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.angleWrap
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
class SimpleGVFController(
    path: Path,
    kN: Double,
    kOmega: Double,
    private val kF: Double,
    epsilon: Double,
    errorMap: (Double) -> Double = { it },
) : GVFController(path, kN, kOmega, epsilon, errorMap) {

    override fun headingControl(gvfVec: Vector2d): Pair<Double, Double> {
        val desiredHeading = gvfVec.angle()
        val headingError = (desiredHeading - lastPose.heading).angleWrap
        return Pair(kOmega * headingError, headingError)
    }

    override fun vectorControl(gvfVec: Vector2d, headingError: Double): Vector2d {
        val projectedDisplacement = (lastS - path.length()).absoluteValue
        var translationalPower = gvfVec * (projectedDisplacement / kF)

        val absoluteDisplacement = path.end().vec() - lastPose.vec()
        isFinished = projectedDisplacement < epsilon && absoluteDisplacement.norm() < epsilon
        val absoluteVector = absoluteDisplacement * (projectedDisplacement / kF)

        if (isFinished) translationalPower = absoluteVector
        if (translationalPower.norm() > 1.0) translationalPower /= translationalPower.norm()

        return translationalPower
    }

    override fun update(currPose: Pose, currVel: Speeds): Speeds {
        lastPose = currPose.toPose2d()
        lastS = if (lastS.isNaN()) {
            path.project(lastPose.vec())
        } else {
            path.fastProject(lastPose.vec(), lastS)
        }

        val vectorFieldResult = GVFUtil.gvfVecAt(path, lastPose, lastS, kN, errorMap)

        val headingResult = headingControl(vectorFieldResult)
        val angularOutput = headingResult.first
        val headingError = headingResult.second

        val vectorResult = vectorControl(vectorFieldResult, headingError)

        val rotated = vectorResult.rotated(PI / 2.0 - lastPose.heading).toVec()
        val speeds = Speeds()
        speeds.setFieldCentric(Pose(rotated, angularOutput))
        return speeds
    }
}
