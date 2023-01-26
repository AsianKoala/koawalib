package com.asiankoala.koawalib.path.gvf

import com.asiankoala.koawalib.control.controller.PIDFController
import com.asiankoala.koawalib.control.controller.PIDGains
import com.asiankoala.koawalib.control.profile.disp.Constraints
import com.asiankoala.koawalib.control.profile.disp.DispState
import com.asiankoala.koawalib.control.profile.disp.OnlineProfile
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.path.Path
import com.asiankoala.koawalib.subsystem.drive.KMecanumOdoDrive
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sign

class BetterMotionProfileGVFController(
    override val path: Path,
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
    private val profile = OnlineProfile(
        DispState(),
        DispState(path.length, 0.0, 0.0),
        constraints
    )
    private var headingError = 0.0
    private val headingController = PIDFController(
        PIDGains(kP = kTheta, kD = kOmega),
        kV,
        kA,
        kS
    )

    var state = DispState() // keep this public for tuning
        private set

    override var s: Double = 0.0
    override val isFinished: Boolean
        get() = path.length - s < epsilon &&
            drive.pose.vec.dist(path.end.vec) < epsilon &&
            headingError.absoluteValue < thetaEpsilon

    private fun tripleProduct(a: Vector, b: Vector, c: Vector) = b * (a dot c) - c * (a dot b)

    private fun calcGvf(): GVFComputation {
        val tangent = path[s, 1].vec
        val normal = tangent.rotate(PI / 2.0)
        val displacementVec = path[s].vec - drive.pose.vec
        val trackingError = displacementVec.norm * (displacementVec cross tangent).sign
        val gvf = tangent - normal * kN * errorMap.invoke(trackingError)
        val unitGvf = gvf.unit
        val xdot = unitGvf * state.v

        // derived in ryan's paper
        val secondDeriv = path[s, 2].vec
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

        val headingDeriv = path[s, 2].heading
        val headingSecondDeriv = path[s, 3].heading
        val thetadot = headingDeriv * projDeriv
        val thetadot2 = headingSecondDeriv * projDeriv * projDeriv + thetadot * projSecondDeriv

        return GVFComputation(
            xdot,
            xdot2,
            thetadot,
            thetadot2
        )
    }

    private fun calcVecFF(vel: Vector, accel: Vector) = vel.unit * kS + vel * kV + accel * kA

    override fun update() {
        s = path.project(drive.pose.vec, s)
        state = profile[s]
        val gvfComputation = calcGvf()
        headingController.apply {
            targetPosition = path[s, 1].heading
            targetVelocity = gvfComputation.angularVel
            targetAcceleration = gvfComputation.angularAccel
        }
        val headingOutput = headingController.update(drive.pose.heading, drive.vel.heading)
        val transOutput = calcVecFF(gvfComputation.transVel, gvfComputation.transAccel)
        val output = Pose(transOutput, headingOutput)
        drive.powers = output
    }
}
