package com.asiankoala.koawalib.pathing

import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.util.Speeds
import kotlin.math.PI
import kotlin.math.sign

abstract class GVFController(
    protected val path: Path,
    protected val kN: Double,
    protected val kOmega: Double,
    protected val epsilon: Double,
    private val errorMap: (Double) -> Double = { it },
) {
    protected var pose: Pose = Pose()
    protected var s: Double = 0.0
    protected var gvfVec = Vector()
    protected var tangent = Vector()
    protected var headingResult = Pair(0.0, 0.0)
    protected var vectorResult = Vector()
    
    var isFinished = false
        protected set

    abstract fun headingControl(): Pair<Double, Double>
    abstract fun vectorControl(): Vector
    abstract fun process(currPose: Pose, currVel: Speeds): Speeds

    /**
     * @param currPose current pose of robot
     * @return robot relative x,y,omega powers
     */
    fun update(currPose: Pose, currVel: Speeds): Speeds {
        pose = currPose
        s = path.project(pose.vec, s)
        gvfVec = gvfVecAt(pose, s).unit
        headingResult = headingControl()
        vectorResult = vectorControl()
        isFinished = path.length - s < epsilon && pose.vec.dist(path.end.vec) < epsilon
        if(isFinished) return Speeds()
        return process(currPose, currVel)
    }

    private fun gvfVecAt(currPose: Pose, currS: Double): Vector {
        tangent = path[currS, 1].vec
        val normal = tangent.rotate(PI / 2.0)
        val displacementVec = path[currS].vec - currPose.vec
        val error = displacementVec.norm * (displacementVec cross tangent).sign
        return tangent - normal * kN * errorMap.invoke(error)
    }
}
