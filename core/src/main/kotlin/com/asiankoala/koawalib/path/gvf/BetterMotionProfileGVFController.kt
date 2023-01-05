package com.asiankoala.koawalib.path.gvf

import com.asiankoala.koawalib.control.controller.PIDFController
import com.asiankoala.koawalib.control.controller.PIDGains
import com.asiankoala.koawalib.control.profile.v2.Constraints
import com.asiankoala.koawalib.control.profile.v2.DispState
import com.asiankoala.koawalib.control.profile.v2.OnlineProfile
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
    private val kV: Double,
    private val kA: Double,
    private val kS: Double
) : GVFController {
    private data class GVFComputation(
        val gvf: Vector,
        val gvfDeriv: Vector,
        val projDeriv: Vector,

    )
    private val profile = OnlineProfile(
        DispState(),
        DispState(path.length, 0.0, 0.0),
        constraints
    )
    private var normal = Vector()
    private var trackingError = 0.0
    private var headingError = 0.0
    private val errorMap: (Double) -> Double = { it }
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

    private fun calcGvf(): Pair<Vector, Vector> {
        val tangent = path[s, 1].vec
        normal = tangent.rotate(PI / 2.0)
        val displacementVec = path[s].vec - drive.pose.vec
        trackingError = displacementVec.norm * (displacementVec cross tangent).sign
        val gvf = tangent - normal * kN * errorMap.invoke(trackingError)
        val unitGvf = gvf.unit

        // derived in ryan's paper
        val secondDeriv = path[s, 2].vec
        val projDeriv = (unitGvf dot tangent) / (1.0 - (displacementVec dot secondDeriv))
        val mapDeriv = 1.0 // TODO change later for non-linear error maps
        val errorDeriv = mapDeriv * (unitGvf dot normal)
        val gvfDeriv = secondDeriv * projDeriv - (secondDeriv.rotate(-PI / 2.0) * trackingError - normal * errorDeriv) * kN
        val unitGvfDeriv = tripleProduct(gvfDeriv, gvfDeriv, gvfDeriv) / (gvfDeriv dot gvfDeriv).pow(3.0 / 2.0)
        return Pair(unitGvf, unitGvfDeriv)
    }

    private fun updateHeadingController() {
        val targetHeading = path[s, 2].heading
        headingController.targetPosition = targetHeading
        headingController.targetVelocity = targetHeading * state.v
    }

    private fun vectorControl(): Pair<Vector, Vector> {
        val gvfResult = calcGvf()
        val vel = gvfResult.first * state.v
        val accel = gvfResult.first * state.a + gvfResult.second * state.v
        return Pair(vel, accel)
    }

    private fun calcVecFF(vel: Vector, accel: Vector) = vel.unit * kS + vel * kV + accel * kA

    // we use a PV controller on the heading beacuse im lazy
    private fun setDriveSetpoints(vel: Vector, accel: Vector) {
        val headingOutput = headingController.update(drive.pose.heading, drive.vel.heading)
        val ffOutput = Pose(calcVecFF(vel, accel), headingOutput)
        drive.powers = ffOutput
    }

    override fun update() {
        s = path.project(drive.pose.vec, s)
        state = profile[s]
        updateHeadingController()
        val vectorResult = vectorControl()
        setDriveSetpoints(vectorResult.first, vectorResult.second)
    }
}
