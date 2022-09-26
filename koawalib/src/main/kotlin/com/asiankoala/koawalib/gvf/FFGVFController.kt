package com.asiankoala.koawalib.gvf

import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.math.degrees
import com.asiankoala.koawalib.util.Speeds
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.max

/**
 * this doesn't work for now so just making it internal till i can fix it
 * also todo: need to figure out better heading management
 */
internal class FFGVFController(
    path: Pathing.Path,
    kN: Double,
    kOmega: Double,
    private val kTheta: Double,
    private val kF: Double,
    private val kLookahead: Double? = null,
    epsilon: Double,
    errorMap: (Double) -> Double = { it },
) : GVFController(path, kN, kOmega, epsilon, errorMap) {

    override fun headingControl(): Pair<Double, Double> {
        var desiredHeading = lastGVFVec.angle

        if (kLookahead != null) {
            val lookaheadS = lastS + kLookahead
            desiredHeading = (path[lookaheadS].vec - path[lastS].vec).angle
        }
        val headingError = (desiredHeading - lastPose.heading).angleWrap
        return Pair(kOmega * headingError, headingError)
    }

    override fun vectorControl(): Vector {
        val projectedDisplacement = (lastS - path.length()).absoluteValue
        var translationalPower = lastGVFVec * (projectedDisplacement / kF)

        val absoluteDisplacement = path.end().vec - lastPose.vec
        isFinished = projectedDisplacement < epsilon && absoluteDisplacement.norm < epsilon
        val absoluteVector = absoluteDisplacement * (projectedDisplacement / kF)

        if (isFinished) translationalPower = absoluteVector
        if (translationalPower.norm > 1.0) translationalPower /= translationalPower.norm

        val thetaWeight = lastHeadingError.degrees.absoluteValue / kTheta
        translationalPower /= max(1.0, thetaWeight)
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

        val rotated = vectorResult.rotate(PI / 2.0 - lastPose.heading)
        val speeds = Speeds()
        speeds.setFieldCentric(Pose(rotated, angularOutput))
        return speeds
    }
}
