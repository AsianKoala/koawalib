package com.asiankoala.koawalib.path

import com.asiankoala.koawalib.math.Point
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Spline
import com.asiankoala.koawalib.math.angleWrap
import kotlin.math.PI
import kotlin.math.sign

class SimpleGVFController(
    private val kF: Double,
    private val kN: Double,
    private val kOmega: Double,
    private val poseSupplier: () -> Pose,
    private val errorMap: (Double) -> Double,
) {
    var splineOptional: Spline? = null

    fun update(spline: Spline): Pose {
        val pose = poseSupplier.invoke()
        val t = spline.getTAtPoint(pose)
        var tangent = spline.getDerivative(t)
        tangent = tangent.scale(1.0 / tangent.norm())
        val normalVector = tangent.rotate(PI / 2.0)
        val displacementVector = spline.evaluate(t).plus(pose.unaryMinus())
        val orientation = Point.cross(displacementVector, tangent).sign
        val error = orientation * displacementVector.norm()
        println("error $error")
        var vectorFieldResult = tangent.plus(normalVector.unaryMinus().scale(kN * errorMap.invoke(error)))
        vectorFieldResult = vectorFieldResult.scale(1.0 / vectorFieldResult.norm())

        val desiredHeading = vectorFieldResult.atan2
        val headingError = (desiredHeading - pose.heading).angleWrap
        val angularOutput = kOmega * headingError

        val distanceToPoint = spline.evaluate(1.0).dist(pose)
        val forwardOutput = kF * distanceToPoint

        val translationalPower = vectorFieldResult.scale(forwardOutput).rotate(pose.heading)

        return Pose(translationalPower, angularOutput)
    }

    fun update(): Pose? {
        return splineOptional?.let { update(it) }
    }
}