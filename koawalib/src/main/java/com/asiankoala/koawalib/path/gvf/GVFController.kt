package com.asiankoala.koawalib.path.gvf

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.path.Path
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.angleWrap
import kotlin.math.PI
import kotlin.math.sign

class GVFController(
    private val kN: Double,
    private val kOmega: Double,
    private val path: Path,
    private val epsilon: Double = 0.2,
    private val kF: Double? = null,
    private val errorMap: (Double) -> Double = { it },
) {

    private var lastS: Double = Double.NaN
    var finished = false
        private set

    fun update(pose: Pose): Pose {
        lastS = if(lastS.isNaN()) {
            path.project(pose.vec())
        } else {
            path.fastProject(pose.vec(), lastS)
        }

        val s = path.project(pose.vec())
        val projectedVec = path[s]
        val tangent = path.deriv(s).toPose().vec().toVec().normalized()
        val normalVec = tangent.rotate(PI / 2.0)
        val displacementVec = projectedVec.vec().toVec().plus(pose.unaryMinus())
        val orientation = (displacementVec cross tangent).sign
        val error = orientation * displacementVec.norm()

        val vectorFieldResult = tangent.plus(normalVec.unaryMinus().scale(kN * errorMap.invoke(error))).normalized()

        val desiredHeading = vectorFieldResult.atan2
        val headingError = (desiredHeading - pose.heading).angleWrap
        val angularOutput = kOmega * headingError

        val distanceToPoint = path.end().toPose().vec().toVec().dist(pose)
        val forwardOutput = kF?.times(distanceToPoint) ?: 1.0

        finished = finished || projectedVec.toPose().vec.dist(path.end().toPose().vec) < epsilon
        val translationalPower = if(finished) {
            (projectedVec.vec().toVec() - pose.vec)
        } else {
            vectorFieldResult
        }.scale(forwardOutput).trueNormal()

        println("pose ${pose.vec} eval $projectedVec power $translationalPower finished $finished forwardOutput $forwardOutput")

        return Pose(translationalPower, angularOutput)
    }
}

fun Pose2d.toPose() = Pose(this.x, this.y, this.heading)
fun Vector2d.toVec() = Vector(this.x, this.y)
