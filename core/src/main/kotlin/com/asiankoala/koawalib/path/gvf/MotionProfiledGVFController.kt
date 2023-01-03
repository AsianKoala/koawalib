package com.asiankoala.koawalib.path.gvf

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

class MotionProfiledGVFController(
    override val path: Path,
    override val drive: KMecanumOdoDrive,
    private val kN: Double,
    private val epsilon: Double,
    private val thetaEpsilon: Double,
    constraints: Constraints,
    private val kOmega: Double,
    private val kS: Double,
    private val kV: Double,
    private val kA: Double,
    private val errorMap: (Double) -> Double = { it }
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

    var state = DispState() // need to be public for tuning
        private set

    override var s: Double = 0.0
    override val isFinished: Boolean
        get() = path.length - s < epsilon &&
            pose.vec.dist(path.end.vec) < epsilon &&
            headingError.absoluteValue < thetaEpsilon

    private fun calcGVF(): Vector {
        val tangent = path[s, 1].vec
        normal = tangent.rotate(PI / 2.0)
        val displacementVec = path[s].vec - pose.vec
        trackingError = displacementVec.norm * (displacementVec cross tangent).sign
        return tangent - normal * kN * errorMap.invoke(trackingError)
    }

    // the vector triple product BAC-CAB identity
    private fun tripleProduct(a: Vector, b: Vector, c: Vector) = b * (a dot c) - c * (a dot b)

    // TODO: fix heading controllers
    // assumes heading is tangent to the path
    // returns velocity
    // h(p(s)) = v'(p(s))
    // h'(p(s)) = v''(p(s)) * p'(s)
    private fun headingControl(): Double {
//        headingError = (path[s].heading - pose.heading).angleWrap.degrees
        return path[s, 2].heading * state.v
    }

    // d/ds v(p(s)) = v'(p(s)) * p'(s)
    // d^2/ds^2 = v''(p(s)) * p'(s) * p'(s) + v(p(s)) * p''(s)
    // now we need to differentiate our gvf
    // v'(s) = t' - kn * (n' * e + e' * n)
    // bc im lazy lets just assume linear error map
    // so e' = 1.0
    // v' = t' - k_n * (n' * e + n)
    // now plug this into d^2/ds^2 and we chilling
    // returns vel and accel vectors
    private fun vectorControl(): Pair<Vector, Vector> {
        val vs = calcGVF()
        val ms = vs.unit
        val dtds = path[s, 2].vec
        val dvds = dtds - (dtds.rotate(PI / 2.0) * trackingError + normal) * kN
        // from https://math.stackexchange.com/a/3826369
        val dmds = tripleProduct(vs, dvds, vs) / vs.norm.pow(3)
        val vel = ms * state.v
        val accel = dmds * state.v * state.v + ms * state.a
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
