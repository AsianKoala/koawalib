package com.asiankoala.koawalib.path.gvf

import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.path.Path
import com.asiankoala.koawalib.util.Speeds
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.sign

abstract class GVFController(
    val path: Path,
    protected val kN: Double,
    protected val epsilon: Double,
    protected val thetaEpsilon: Double,
    protected val errorMap: (Double) -> Double = { it },
) {
    var isFinished = false
        protected set

    protected var pose: Pose = Pose()
    protected var s: Double = 0.0
    protected var gvfVec = Vector()
    protected var md = Vector()
    protected var tangent = Vector()
    protected var normal = Vector()
    protected var error = 0.0
    protected var headingResult = Pair(0.0, 0.0)
    protected var vectorResult = Vector()

    abstract fun headingControl(): Pair<Double, Double>
    abstract fun vectorControl(): Vector

    /**
     * @param currPose current pose of robot
     * @param currVel current vel of robot
     * @return robot relative x,y,omega powers
     */
    open fun update(currPose: Pose): Speeds {
        pose = currPose
        s = path.project(pose.vec, s)
        gvfVec = gvfVecAt()
        md = gvfVec.unit
        headingResult = headingControl()
        vectorResult = vectorControl()
        isFinished = path.length - s < epsilon &&
            pose.vec.dist(path.end.vec) < epsilon &&
            headingResult.second.absoluteValue < thetaEpsilon
        val speeds = Speeds()
        speeds.setFieldCentric(Pose(vectorResult, headingResult.first))
        return speeds
    }

    protected fun gvfVecAt(): Vector {
        tangent = path[s, 1].vec
        normal = tangent.rotate(PI / 2.0)
        val displacementVec = path[s].vec - pose.vec
        error = displacementVec.norm * (displacementVec cross tangent).sign
        Logger.logInfo("s: $s, d: $tangent, r: $displacementVec, e: $error")
        return tangent - normal * kN * errorMap.invoke(error)
    }
}
