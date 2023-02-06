package com.asiankoala.koawalib.path.gvf

import com.acmerobotics.roadrunner.util.DoubleProgression
import com.asiankoala.koawalib.control.controller.PIDFController
import com.asiankoala.koawalib.control.controller.PIDGains
import com.asiankoala.koawalib.control.profile.disp.*
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.path.HermitePath
import com.asiankoala.koawalib.subsystem.drive.KMecanumOdoDrive
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sign

class BetterMotionProfileGVFController(
    override val path: HermitePath,
    override val drive: KMecanumOdoDrive,
    private val kN: Double,
    kTheta: Double,
    kOmega: Double,
    private val epsilon: Double,
    private val thetaEpsilon: Double,
    constraints: Constraints,
    private val kS: Double,
    private val kV: Double,
    private val kA: Double,
    private val errorMap: (Double) -> Double = { it },
    private val errorMapDeriv: (Double) -> Double = { 1.0 }
) : GVFController {
    private data class GVFComputation(
        val transVel: Vector,
        val transAccel: Vector,
        val angularVel: Double,
        val angularAccel: Double
    )
    private val profile = OnlineMotionProfile(
        DisplacementState(0.0),
        DisplacementState(0.0),
        path.length,
        object : MotionConstraints() {
            override fun get(s: Double) = SimpleMotionConstraints(constraints.vel, constraints.accel)
            override fun get(s: DoubleProgression) = s.map { get(it) }
        },
    )
    private var headingError = 0.0
    private val headingController = PIDFController(
        PIDGains(kP = kTheta, kD = kOmega),
    )

    var state = DisplacementState(0.0) // keep this public for tuning
        private set

    override var disp: Double = 0.0
    override val isFinished: Boolean
        get() = path.length - disp < epsilon &&
            drive.pose.vec.dist(path.end.vec) < epsilon &&
            headingError.absoluteValue < thetaEpsilon

    private fun tripleProduct(a: Vector, b: Vector, c: Vector) = b * (a dot c) - c * (a dot b)

    private data class HenoResult(
        val vec: Vector,
        val deriv: Vector,
        val projDeriv: Double,
        val error: Double
    )
    private fun henoCalcGVF(): HenoResult {
        val pathPoint = path[disp].vec
        val pathDeriv = path[disp, 1].vec
        val pathSecondDeriv = path[disp, 2].vec

        val normal = pathDeriv.rotate(PI / 2.0)
        val pathToPoint = drive.pose.vec - pathPoint
        val error = pathToPoint dot normal
        val vector = pathDeriv - normal * kN * errorMap.invoke(error)
        val unitVector = vector.unit
        val projDeriv = (unitVector dot pathDeriv) / (1.0 - (pathToPoint dot pathSecondDeriv))
        val errorDeriv = errorMapDeriv.invoke(error) * (unitVector dot normal)
        val tangentDeriv = pathSecondDeriv * projDeriv
        val normalDeriv = tangentDeriv.rotate(-PI / 2.0)
        val vectorDeriv = (tangentDeriv - (normalDeriv * kN * errorMapDeriv.invoke(error))) - (normal * kN * errorDeriv)
        val unitVectorDeriv = (vectorDeriv * (vector dot vector) - vector * (vectorDeriv dot vector)) / (vector.norm * vector.norm * vector.norm)
        return HenoResult(
            unitVector,
            unitVectorDeriv,
            projDeriv,
            error
        )
    }

    private fun calcGvf(): GVFComputation {
        val tangent = path[disp, 1].vec
        val normal = tangent.rotate(PI / 2.0)
        val displacementVec = path[disp].vec - drive.pose.vec
        val trackingError = displacementVec.norm * (displacementVec cross tangent).sign
        val gvf = tangent - normal * kN * errorMap.invoke(trackingError)
        val unitGvf = gvf.unit
        val xdot = unitGvf * state.v

        // derived in ryan's paper
        val secondDeriv = path[disp, 2].vec
        val projDeriv = (xdot dot tangent) / (1.0 - (displacementVec dot secondDeriv))
        val mapDeriv = errorMapDeriv.invoke(trackingError)
        val errorDeriv = mapDeriv * (xdot dot normal)
        val gvfDeriv = secondDeriv * projDeriv - (secondDeriv.rotate(-PI / 2.0) * trackingError - normal * errorDeriv) * kN
        val unitGvfDeriv = tripleProduct(gvfDeriv, gvfDeriv, gvfDeriv) / (gvfDeriv dot gvfDeriv).pow(3.0 / 2.0)
        val xdot2 = unitGvf * state.a + unitGvfDeriv * state.v

        val projSecondDenom = 1.0 - (displacementVec dot secondDeriv)
        val lhsNum = ((xdot * projDeriv) dot secondDeriv) + (xdot2 dot tangent)
        val rhsNum = (xdot dot tangent) * (xdot dot secondDeriv)
        val projSecondDeriv = (lhsNum / projSecondDenom) + (rhsNum / projSecondDenom.pow(2))

//        val headingDeriv = path[s, 2].heading
//        val headingSecondDeriv = path[s, 3].heading
//        val thetadot = headingDeriv * projDeriv
//        val thetadot2 = headingSecondDeriv * projDeriv * projDeriv + thetadot * projSecondDeriv
        val thetadot = 0.0
        val thetadot2 = 0.0

        return GVFComputation(
            xdot,
            xdot2,
            thetadot,
            thetadot2
        )
    }

    private fun calcVecFF(vel: Vector, accel: Vector) = vel.unit * kS + vel * kV + accel * kA

    override fun update() {
        disp = path.project(drive.pose.vec, disp)
        state = profile[disp]
        profile.update(state.v)
//        val gvfComputation = calcGvf()
        val gvfRes = henoCalcGVF()
        val gvfVector = if(isFinished) (path[disp].vec - drive.pose.vec).unit else gvfRes.vec
        val gvfDeriv = if(isFinished) Vector() else gvfRes.deriv
        val xdot = gvfVector * state.v
        val xdot2 = gvfVector * state.a + gvfDeriv * state.v
        headingController.apply {
            targetPosition = path[disp, 1].heading
//            targetVelocity = gvfComputation.angularVel
//            targetAcceleration = gvfComputation.angularAccel
        }
        val headingOutput = headingController.update(drive.pose.heading, drive.vel.heading)
        val transOutput = calcVecFF(xdot, xdot2).rotate(PI / 2.0 - drive.pose.heading)
        val output = Pose(transOutput, headingOutput)
        Logger.logInfo("pos: ${drive.pose.vec}, gvf: $gvfVector, disp: $disp, v: ${state.v}, a: ${state.a}, trans: ${state.v}")
        drive.powers = output
    }
}
