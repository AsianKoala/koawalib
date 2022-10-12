package com.asiankoala.koawalib.path.gvf

import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.path.Path
import com.asiankoala.koawalib.util.Speeds
import kotlin.math.PI
import kotlin.math.sign

abstract class GVFController(
    protected val path: Path,
    protected val kN: Double,
    protected val kOmega: Double,
    private val epsilon: Double,
    private val errorMap: (Double) -> Double = { it },
) {
    var isFinished = false
        private set

    protected var pose: Pose = Pose()
    protected var s: Double = 0.0
    protected var gvfVec = Vector()
    protected var tangent = Vector()
    protected var headingResult = Pair(0.0, 0.0)
    private var vectorResult = Vector()

    abstract fun headingControl(vel: Speeds): Pair<Double, Double>
    abstract fun vectorControl(vel: Speeds): Vector

    /**
     * @param currPose current pose of robot
     * @param currVel current vel of robot
     * @return robot relative x,y,omega powers
     */
    fun update(currPose: Pose, currVel: Speeds): Speeds {
        pose = currPose
        s = path.project(pose.vec, s)
        gvfVec = gvfVecAt().unit
        headingResult = headingControl(currVel)
        vectorResult = vectorControl(currVel)
        isFinished = path.length - s < epsilon && pose.vec.dist(path.end.vec) < epsilon
        val speeds = Speeds()
        speeds.setFieldCentric(Pose(vectorResult, headingResult.first))
        return speeds
    }

    private fun gvfVecAt(): Vector {
        tangent = path[s, 1].vec
        val normal = tangent.rotate(PI / 2.0)
        val displacementVec = path[s].vec - pose.vec
        val error = displacementVec.norm * (displacementVec cross tangent).sign
        Logger.logInfo("s: $s, d: $tangent, r: $displacementVec, e: $error")
        return tangent - normal * kN * errorMap.invoke(error)
    }
}
