package com.asiankoala.koawalib.gvf

import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.path.Path
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.math.degrees
import com.asiankoala.koawalib.math.radians
import com.asiankoala.koawalib.util.Speeds
import kotlin.math.PI
import kotlin.math.absoluteValue

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
    path: Path,
    kN: Double,
    kOmega: Double,
    private val kF: Double,
    private val kS: Double,
    epsilon: Double,
    errorMap: (Double) -> Double = { it },
) : GVFController(path, kN, kOmega, epsilon, errorMap) {

    override fun headingControl(): Pair<Double, Double> {
        val desiredHeading = lastTangentVec.angle() // TODO: don't be a bitch
//        val desiredHeading = lastGVFVec.angle() // TODO: don't be a bitch
        val headingError = (desiredHeading - lastPose.heading).angleWrap.degrees
        Logger.logInfo("target heading", desiredHeading.degrees)
        Logger.logInfo("curr heading", lastPose.heading.degrees)
        val result = kOmega * headingError
        return Pair(result, headingError)
    }

    override fun vectorControl(): Vector2d {
        val paramTillEnd = path.length() - lastS
        var translationalPower = (lastGVFVec / lastGVFVec.norm()) * kS
        if(paramTillEnd < kF) translationalPower /= kF

        val endRVector = path.end().vec() - lastPose.vec()
        isFinished = paramTillEnd < epsilon && endRVector.norm() < epsilon
        if(isFinished) return Vector2d()

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

        val vectorFieldResult = gvfVecAt(lastPose, lastS)

        lastGVFVec = vectorFieldResult

        val headingResult = headingControl()
        val angularOutput = headingResult.first
        val headingError = headingResult.second

        Logger.logInfo("gvf", lastGVFVec / lastGVFVec.norm())

        lastHeadingError = headingError

        val vectorResult = vectorControl()

        val speeds = Speeds()
        speeds.setFieldCentric(Pose(vectorResult.toVec(), angularOutput))
        return speeds
    }
}
