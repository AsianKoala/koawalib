package com.asiankoala.koawalib.path.gvf

import com.acmerobotics.roadrunner.geometry.Pose2d
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
    private val kF: Double? = null,
    private val epsilon: Double = 0.2,
    private val errorMap: (Double) -> Double = { it },
) {

    private var lastS: Double = Double.NaN
    var finished = false
        private set

    fun update(pose: Pose): Pose {
        lastS = if(lastS.isNaN()) {
            path.project(pose.toVec2d())
        } else {
            path.fastProject(pose.toVec2d(), lastS)
        }

        val s = path.project(pose.toVec2d())
        val tangent = path.deriv(s).toVec()
        val normalVec = tangent.rotate(PI / 2.0)

        val projectedVec = path[s]
        val displacementVec = projectedVec.toVec().minus(pose.vec)
        val orientation = displacementVec cross tangent
        val error = displacementVec.norm() * orientation.sign

        val vectorFieldResult = tangent - normalVec.scale(kN * errorMap.invoke(error))

        val desiredHeading = vectorFieldResult.atan2
        val headingError = (desiredHeading - pose.heading).angleWrap
        val angularOutput = kOmega * headingError

        val endDisplacement = (s - path.length()).absoluteValue
        val forwardOutput = if(kF == null) 1.0 else endDisplacement / kF

        finished = finished || endDisplacement < epsilon
        val translationalPower = if(finished) {
            (projectedVec.toVec() - pose.vec)
        } else {
            vectorFieldResult
        }.scale(forwardOutput).clampNormalized()

        println("pose $pose, finished $finished, error $error, projected, $projectedVec, displacement $displacementVec, translationalPower $translationalPower, endDistance $endDisplacement")

        return Pose(translationalPower, angularOutput)
    }
}

fun Vector2d.toVec() = Vector(this.x, this.y)
fun Pose2d.toVec() = this.vec().toVec()
fun Pose.toVec2d() = Vector2d(x, y)
fun Pose.vec(): Vector = Vector(x, y)
