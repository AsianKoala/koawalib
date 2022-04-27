package com.asiankoala.koawalib.gvf

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.path.Path
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.math.degrees
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.sign

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
class GVFController(
    private val path: Path,
    private val kN: Double,
    private val kOmega: Double,
    private val kTheta: Double,
    private val kF: Double,
    private val epsilon: Double,
    private val errorMap: (Double) -> Double = { it },
) {
    private var lastS: Double = Double.NaN
    var isFinished = false
        private set

    private fun Vector2d.toVec() = Vector(this.x, this.y)

    private fun gvfVecAt(pose: Pose2d): Vector2d {
        val s = lastS
        val tangentVec = path.deriv(s).vec()
        val normalVec = tangentVec.rotated(PI / 2.0)
        val projected = path[s].vec()
        val displacementVec = projected - pose.vec()
        val orientation = displacementVec.toVec() cross tangentVec.toVec()
        val error = displacementVec.norm() * orientation.sign
        return tangentVec - normalVec * kN * errorMap.invoke(error)
    }

    /**
     * @param currPose current pose of robot
     * @return a Pair of field relative and robot relative x,y,omega powers
     */
    fun update(currPose: Pose, heading: Double? = null): Pair<Pose, Pose> {
        val pose = currPose.toPose2d()
        lastS = if (lastS.isNaN()) {
            path.project(pose.vec())
        } else {
            path.fastProject(pose.vec(), lastS)
        }

        val vectorFieldResult = gvfVecAt(pose)

        val desiredHeading = heading ?: vectorFieldResult.angle()
        val headingError = (desiredHeading - pose.heading).angleWrap
        val angularOutput = kOmega * headingError

        val projectedDisplacement = (lastS - path.length()).absoluteValue
        var translationalPower = vectorFieldResult * (projectedDisplacement / kF)

        val absoluteDisplacement = path.end().vec() - pose.vec()
        isFinished = projectedDisplacement < epsilon && absoluteDisplacement.norm() < epsilon
        val absoluteVector = absoluteDisplacement * (projectedDisplacement / kF)

        if (isFinished) translationalPower = absoluteVector
        if (translationalPower.norm() > 1.0) translationalPower /= translationalPower.norm()

        val thetaWeight = headingError.degrees.absoluteValue / kTheta
        translationalPower /= max(1.0, thetaWeight)

        val rotated = translationalPower.rotated(PI / 2.0 - pose.heading).toVec()
        return Pair(Pose(translationalPower.toVec(), angularOutput), Pose(rotated, angularOutput))
    }
}
