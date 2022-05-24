package com.asiankoala.koawalib.gvf

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.path.Path
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
    protected val errorMap: (Double) -> Double = { it },
) {
    protected var lastPose: Pose2d = Pose2d()
    protected var lastS: Double = Double.NaN
    protected var lastGVFVec = Vector2d()
    protected var lastHeadingError = 0.0
    var isFinished = false
        protected set

    abstract fun headingControl(): Pair<Double, Double>
    abstract fun vectorControl(): Vector2d

    /**
     * @param currPose current pose of robot
     * @return robot relative x,y,omega powers
     */
    abstract fun update(currPose: Pose, currVel: Speeds): Speeds

    protected fun Vector2d.toVec() = Vector(this.x, this.y)

    protected fun gvfVecAt(pose: Pose2d, s: Double): Vector2d {
        val tangentVec = path.deriv(s).vec()
        val normalVec = tangentVec.rotated(PI / 2.0)
        val projected = path[s].vec()
        val displacementVec = projected - pose.vec()
        val orientation = displacementVec.toVec() cross tangentVec.toVec()
        val error = displacementVec.norm() * orientation.sign
        return tangentVec - normalVec * kN * errorMap.invoke(error)
    }
}
