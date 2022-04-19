package com.asiankoala.koawalib.path.gvf

import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.path.Path
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.angleWrap
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.sign

class GVFController(
    private val path: Path,
    private val kN: Double,
    private val kOmega: Double,
    private val kF: Double,
    private val kEnd: Double,
    private val errorMap: (Double) -> Double = { it },
) {

    private var lastS: Double = Double.NaN
    var isFinished = false
        private set

    private fun Vector2d.toVec() = Vector(this.x, this.y)

    /**
     * @param currPose current pose of robot
     * @return a Pair of field relative and robot relative x,y,omega powers
     */
    fun update(currPose: Pose): Pair<Pose, Pose> {
        val pose = currPose.toPose2d()
        lastS = if(lastS.isNaN()) {
            path.project(pose.vec())
        } else {
            path.fastProject(pose.vec(), lastS)
        }

        val s = lastS
        val tangentVec = path.deriv(s).vec()
        val normalVec = tangentVec.rotated(PI / 2.0)
        val projected = path[s].vec()
        val displacementVec = projected - pose.vec()
        val orientation = displacementVec.toVec() cross tangentVec.toVec()
        val error = displacementVec.norm() * orientation.sign
        val vectorFieldResult = tangentVec - (normalVec * (kN * errorMap.invoke(error)))

        val optimalHeading = vectorFieldResult.angle()
        val headingError = (optimalHeading - pose.heading).angleWrap
        val angularOutput = kOmega * headingError

        val projectedDisplacement = (s - path.length()).absoluteValue
        var translationalPower = vectorFieldResult * (projectedDisplacement / kF)

        val absoluteDisplacement = path.end().vec() - pose.vec()
        isFinished = projectedDisplacement < kEnd && absoluteDisplacement.norm() < kEnd
        val absoluteVector = absoluteDisplacement * (projectedDisplacement / kF)

        if(isFinished) translationalPower =  absoluteVector
        if(translationalPower.norm() > 1.0) translationalPower /= translationalPower.norm()

        val rotated = translationalPower.rotated(PI / 2.0 - pose.heading)
        return Pair(Pose(translationalPower.toVec(), angularOutput), Pose(rotated.toVec(), angularOutput))
    }
}

