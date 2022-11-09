package com.asiankoala.koawalib.path.gvf

import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.math.degrees
import com.asiankoala.koawalib.path.Path
import com.asiankoala.koawalib.util.Speeds
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sign

class MotionProfiledGVFController(
    private val path: Path,
    private val kN: Double,
    private val epsilon: Double,
    private val thetaEpsilon: Double,
    constraints: Constraints,
    private val kOmega: Double,
    private val kStatic: Double,
    private val kV: Double,
    private val kA: Double,
    private val kIsTuning: Boolean = false,
    private val errorMap: (Double) -> Double = { it }
) : GVFController {
    private var pose: Pose = Pose()
    private var s: Double = 0.0
    private val profile = OnlineProfile(
        DispState(),
        DispState(path.length, 0.0, 0.0),
        constraints
    )
    private var state = DispState()
    private var normal = Vector()
    private var trackingError = 0.0
    private var headingError = 0.0

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

    // lol i'm lazy af. just a simple p controller our target heading
    // might be enough? to fix tho i would have to rework how my pathing system
    // deals with heading. just needs a bit of testing tbh
    private fun headingControl(): Pair<Double, Double> {
        headingError = (path[s].heading - pose.heading).angleWrap.degrees
        return Pair(headingError / kOmega * state.v, headingError)
    }

    // d/ds v(p(s)) = v'(p(s)) * p'(s)
    // d^2/ds^2 = v''(p(s)) * p'(s) * p'(s) + v(p(s)) * p''(s)
    // now we need to differentiate our gvf
    // v'(s) = t' - kn * (n' * e + e' * n)
    // bc im lazy lets just assume linear error map
    // so e' = 1.0
    // v' = t' - k_n * (n' * e + n)
    // now plug this into d^2/ds^2 and we chilling
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

    private fun calcVecFF(vel: Vector, accel: Vector) = vel.unit * kStatic + vel * kV + accel * kA
    private fun calcHeadingFF(vel: Double) = kStatic * vel.sign + vel * kV

    override fun update(currPose: Pose): Pose {
        pose = currPose
        s = path.project(pose.vec, s)
        state = profile[s]
        if (kIsTuning) Logger.addVar("target velocity", state.v)
        val headingResult = headingControl()
        val vectorResult = vectorControl()
        return Speeds().apply {
            setFieldCentric(
                Pose(
                    calcVecFF(
                        vectorResult.first,
                        vectorResult.second
                    ),
                    calcHeadingFF(headingResult.first)
                )
            )
        }.getRobotCentric(pose.heading)
    }
}
