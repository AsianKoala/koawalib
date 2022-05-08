package com.asiankoala.koawalib.gvf

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.path.Path
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector

abstract class GVFController(
    protected val path: Path,
    protected val kN: Double,
    protected val kOmega: Double,
    protected val kTheta: Double,
    protected val kF: Double,
    protected val epsilon: Double,
    protected val errorMap: (Double) -> Double = { it },
) {
    protected var lastPose: Pose2d = Pose2d()
    protected var lastS: Double = Double.NaN
    var isFinished = false
        protected set

    abstract fun headingControl(vararg params: Any): Pair<Double, Double>
    abstract fun vectorControl(vararg params: Any): Vector2d

    /**
     * @param currPose current pose of robot
     * @return robot relative x,y,omega powers
     */
    abstract fun update(currPose: Pose): Pose
}