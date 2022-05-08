package com.asiankoala.koawalib.gvf

import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.path.Path
import com.asiankoala.koawalib.control.motion.MotionConstraints
import com.asiankoala.koawalib.control.motion.MotionProfile
import com.asiankoala.koawalib.control.motion.MotionState
import com.asiankoala.koawalib.gvf.GVFUtil.toVec
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.math.clamp
import com.asiankoala.koawalib.math.estimateDerivative
import com.asiankoala.koawalib.util.Speeds
import com.qualcomm.robotcore.util.ElapsedTime
import kotlin.math.*

class MPGVFController(
    path: Path,
    kN: Double,
    kOmega: Double,
    epsilon: Double,
    errorMap: (Double) -> Double = { it },
    translationalConstraints: MotionConstraints,
    private val vecKf: Double,
    private val vecKp: Double,
    private val headingKf: Double,
    private val headingKp: Double
) : GVFController(path, kN, kOmega, epsilon, errorMap) {
    private val startState = MotionState()
    private val endState = MotionState(path.length())
    private val profile = MotionProfile(startState, endState, translationalConstraints)
    private val timer = ElapsedTime()
    private var lastSeconds: Double? = null
    private val state get() = profile[lastSeconds ?: 0.0]
    private var crossTrackErrors = ArrayList<Pair<Double, Double>>()

    override fun headingControl(gvfVec: Vector2d): Pair<Double, Double> {
        /**
         * so theoretically the heading deriv of a gvf SHOULD be numerically calculated
         * since idk if what i'm doing is mathematically accurate
         * but fuck it lets just assume this works
         * t(s) = tangent of curve at s
         * n(s) = normal of curve at s
         * e(d) = kN * errorMap(d)
         * ASSUMING LINEAR ERROR MAP!!! (otherwise math gets really messy)
         * d(s) = displacement at s
         * e(d) = kN * d(s)
         * u'(s) = t'(s) - (n'(s)e(d) + n(s)e'(d))
         *
         * t'(s) = second deriv of curve at s
         * n'(s) = second deriv rotated 90
         * e'(d) = kN * d'(s)
         * d'(s) = approximation of d/dt (cross track error)
         *
         * and then finally target heading vel = kOmega * motionState.v * u'(s).angle()
         */

        val secondDeriv = path.secondDeriv(lastS).vec()

        fun derivRotate(v: Vector2d, angle: Double): Vector2d {
            val newX = v.x * -sin(angle) - v.y * cos(angle)
            val newY = v.x * cos(angle) + v.y * -sin(angle)
            return Vector2d(newX, newY)
        }

        val normalSecondDeriv = derivRotate(secondDeriv, PI / 2.0)

        val displacement = path[lastS].vec() - lastPose.vec()
        val orientation = displacement.toVec() cross secondDeriv.toVec()
        val error = displacement.norm() * orientation.sign
        val crossTrackCorrection = kN * errorMap.invoke(error)
        crossTrackErrors.add(Pair(timer.seconds(), crossTrackCorrection))

        val derivOutput = estimateDerivative(crossTrackErrors)

        if(!derivOutput.second) {
            val desiredHeading = gvfVec.angle()
            val headingError = (desiredHeading - lastPose.heading).angleWrap
            return Pair(kOmega * headingError, headingError)
        }

        val crossTrackDeriv = derivOutput.first

        val tangent = path.deriv(lastS).vec()
        val normal = tangent.rotated(PI / 2.0)
        val output = secondDeriv - (normalSecondDeriv * error + normal * crossTrackDeriv)

        return Pair(kOmega * output.angle() * state.v, Double.NaN)
    }

    override fun vectorControl(gvfVec: Vector2d, headingError: Double): Vector2d {
        val projectedDisplacement = (lastS - path.length()).absoluteValue
        var translationalPower = gvfVec

        val absoluteDisplacement = path.end().vec() - lastPose.vec()
        isFinished = projectedDisplacement < epsilon && absoluteDisplacement.norm() < epsilon

        if (isFinished) translationalPower = absoluteDisplacement
        if (translationalPower.norm() > 1.0) translationalPower /= translationalPower.norm()

        return translationalPower
    }

    override fun update(currPose: Pose, currVel: Speeds): Speeds {
        lastPose = currPose.toPose2d()

        if(lastSeconds == null) {
            timer.reset()
        }

        lastSeconds = timer.seconds()
        lastS = if (lastS.isNaN()) {
            path.project(lastPose.vec())
        } else {
            path.fastProject(lastPose.vec(), lastS)
        }

        val vectorFieldResult = GVFUtil.gvfVecAt(path, lastPose, lastS, kN, errorMap)

        val targetHeadingVel = headingControl(vectorFieldResult).first

        val vectorResult = vectorControl(vectorFieldResult, Double.NaN)
        val targetVecVel = (vectorResult * state.v).toVec()

        val targetSpeeds = Speeds()
        targetSpeeds.setFieldCentric(Pose(targetVecVel, targetHeadingVel))

        val currSpeeds = Speeds()
        currSpeeds.setFieldCentric(currVel.getFieldCentric())

        val errorSpeeds = Speeds()
        errorSpeeds.setFieldCentric(
            Pose(
                targetSpeeds.getFieldCentric().vec - currSpeeds.getFieldCentric().vec,
                targetSpeeds.getFieldCentric().heading - currSpeeds.getFieldCentric().heading
            )
        )

        val robotCentricTarget = targetSpeeds.getRobotCentric(lastPose.heading)
        val robotCentricErrors = errorSpeeds.getRobotCentric(lastPose.heading)

        fun fp(target: Double, error: Double, kF: Double, kP: Double): Double {
            return clamp(target * kF + error * kP, -1.0, 1.0)
        }

        val xOutput = fp(robotCentricTarget.x, robotCentricErrors.x, vecKf, vecKp)
        val yOutput = fp(robotCentricTarget.y, robotCentricErrors.y, vecKf, vecKp)
        val headingOutput = fp(robotCentricTarget.heading, robotCentricErrors.heading, headingKf, headingKp)
        val outputVec = Pose(xOutput, yOutput, headingOutput)

        val outputSpeeds = Speeds()
        outputSpeeds.setRobotCentric(outputVec, lastPose.heading)

        return outputSpeeds
    }
}