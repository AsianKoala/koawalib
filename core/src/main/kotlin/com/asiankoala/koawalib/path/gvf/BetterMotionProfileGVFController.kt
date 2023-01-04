package com.asiankoala.koawalib.path.gvf

import com.asiankoala.koawalib.control.profile.v2.Constraints
import com.asiankoala.koawalib.control.profile.v2.DispState
import com.asiankoala.koawalib.control.profile.v2.OnlineProfile
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.math.degrees
import com.asiankoala.koawalib.path.Path
import com.asiankoala.koawalib.subsystem.drive.KMecanumOdoDrive
import com.asiankoala.koawalib.util.Speeds
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sign

class BetterMotionProfileGVFController(
    override val path: Path,
    override val drive: KMecanumOdoDrive,
    private val kN: Double,
    private val kOmega: Double,
    private val epsilon: Double,
    private val thetaEpsilon: Double,
    constraints: Constraints,
    private val kS: Double,
    private val kV: Double,
    private val kA: Double,
) : GVFController {
    private var pose: Pose = Pose()
    private val profile = OnlineProfile(
        DispState(),
        DispState(path.length, 0.0, 0.0),
        constraints
    )
    private var normal = Vector()
    private var trackingError = 0.0
    private var headingError = 0.0
    private val errorMap: (Double) -> Double = { it }

    var state = DispState() // keep this public for tuning
        private set

    override var s: Double = 0.0
    override val isFinished: Boolean
        get() = path.length - s < epsilon &&
                pose.vec.dist(path.end.vec) < epsilon &&
                headingError.absoluteValue < thetaEpsilon

    private fun tripleProduct(a: Vector, b: Vector, c: Vector) = b * (a dot c) - c * (a dot b)

    private fun calcGvf(): Pair<Vector, Vector> {
        val tangent = path[s, 1].vec
        normal = tangent.rotate(PI / 2.0)
        val displacementVec = path[s].vec - pose.vec
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

    private fun headingControl() = path[s, 2].heading * state.v

    private fun vectorControl(): Pair<Vector, Vector> {
        val gvfResult = calcGvf()
        val vel = gvfResult.first * state.v
        val accel = gvfResult.first * state.a + gvfResult.second * state.v
        return Pair(vel, accel)
    }

    private fun calcVecFF(vel: Vector, accel: Vector) = vel.unit * kS + vel * kV + accel * kA
    private fun calcHeadingFF(vel: Double) = kS * vel.sign + vel * kV

    // we use a PV controller on the heading beacuse im lazy
    private fun setDriveSetpoints(vel: Vector, accel: Vector, headingVel: Double) {
        val headingOutput = headingError * kOmega + calcHeadingFF(headingVel)
        val ffOutput = Pose(calcVecFF(vel, accel), headingOutput)
        drive.powers = ffOutput
    }

    override fun update() {
        pose = drive.pose
        s = path.project(pose.vec, s)
        state = profile[s]
        val vectorResult = vectorControl()
        val headingVel = headingControl()
        setDriveSetpoints(vectorResult.first, vectorResult.second, headingVel)
    }
}
