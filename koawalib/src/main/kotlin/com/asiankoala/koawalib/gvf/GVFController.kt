package com.asiankoala.koawalib.gvf

import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.util.Speeds
import kotlin.math.PI
import kotlin.math.sign

abstract class GVFController(
    protected val path: Pathing.Path,
    protected val kN: Double,
    protected val kOmega: Double,
    protected val epsilon: Double,
    protected val errorMap: (Double) -> Double = { it },
) {
    protected var lastPose: Pose = Pose()
    protected var lastS: Double = Double.NaN
    protected var lastGVFVec = Vector()
    protected var lastTangentVec = Vector()
    protected var lastHeadingError = 0.0
    var isFinished = false
        protected set

    abstract fun headingControl(): Pair<Double, Double>
    abstract fun vectorControl(): Vector

    /**
     * @param currPose current pose of robot
     * @return robot relative x,y,omega powers
     */
    abstract fun update(currPose: Pose, currVel: Speeds): Speeds

    protected fun gvfVecAt(pose: Pose, s: Double): Vector {
        val tangentVec = path.deriv(s).vec
        lastTangentVec = tangentVec
        val normalVec = tangentVec.rotate(PI / 2.0)
        val projected = path[s].vec
        val displacementVec = projected - pose.vec
        val orientation = displacementVec cross tangentVec
        val error = displacementVec.norm * orientation.sign
        return tangentVec - normalVec * kN * errorMap.invoke(error)
    }
}
