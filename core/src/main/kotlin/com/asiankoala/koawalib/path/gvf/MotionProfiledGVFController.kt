package com.asiankoala.koawalib.path.gvf

import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.math.degrees
import com.asiankoala.koawalib.path.Path
import com.asiankoala.koawalib.subsystem.drive.KMecanumDrive
import com.asiankoala.koawalib.util.Speeds
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sign

// pretty mid motion profiled gvf
// NEEDS TESTING!!!
class MotionProfiledGVFController(
    path: Path,
    kN: Double,
    epsilon: Double,
    thetaEpsilon: Double,
    constraints: Constraints,
    private val kStatic: Double,
    private val kV: Double,
    private val kA: Double,
    private val kOmega: Double,
    private val kIsTuning: Boolean = false,
    errorMap: (Double) -> Double = { it },
) : GVFController(path, kN, epsilon, thetaEpsilon, errorMap) {
    private val profile = OnlineProfile(
        DispState(),
        DispState(path.length, 0.0, 0.0),
        constraints
    )

    private var state = DispState()

    // basic feedforward
    private fun feedforwardMap(velAccel: Pair<Double, Double>) =
        kStatic * velAccel.first.sign + velAccel.first * kV + velAccel.second * kA

    // the vector triple product BAC-CAB identity
    private fun tripleProduct(a: Vector, b: Vector, c: Vector) = b * (a dot c) - c * (a dot b)

    // lol i'm lazy af. just a simple p controller our target heading
    // might be enough? to fix tho i would have to rework how my pathing system
    // deals with heading. just needs a bit of testing tbh
    override fun headingControl(): Pair<Double, Double> {
        val error = (path[s].heading - pose.heading).angleWrap.degrees
        return Pair(error * kOmega * state.v, error)
    }

    // d/ds v(p(s)) = v'(p(s)) * p'(s)
    // d^2/ds^2 = v''(p(s)) * p'(s) * p'(s) + v(p(s)) * p''(s)
    // now we need to differentiate our gvf
    // v'(s) = t' - kn * (n' * e + e' * n)
    // bc im lazy lets just assume linear error map
    // so e' = 1.0
    // v' = t' - k_n * (n' * e + n)
    override fun vectorControl(): Vector {
        val vel = md * state.v
        val dvds = path[s, 2].vec - (path[s, 2].vec.rotate(PI / 2.0) * error + normal) * kN
        // from https://math.stackexchange.com/questions/2983445/unit-vector-differentiation
        val mdDot = tripleProduct(gvfVec, dvds, gvfVec) / gvfVec.norm.pow(3)
        val accel = mdDot * state.v * state.v + md * state.a
        val vels = KMecanumDrive.mecKinematics(Pose(vel, 0.0))
        val accels = KMecanumDrive.mecKinematics(Pose(accel, 0.0))
        val powers = vels.zip(accels).map(::feedforwardMap) // kotlin syntax so clean
        val res = Speeds().apply { setWheels(powers, pose.heading) }
        if (kIsTuning) Logger.addVar("target velocity", state.v)
        return res.getFieldCentric().vec
    }

    override fun update(currPose: Pose): Speeds {
        pose = currPose
        s = path.project(pose.vec, s)
        state = profile[s]
        gvfVec = gvfVecAt()
        md = gvfVec.unit
        headingResult = headingControl()
        vectorResult = vectorControl()
        isFinished = path.length - s < epsilon &&
            pose.vec.dist(path.end.vec) < epsilon &&
            headingResult.second.absoluteValue < thetaEpsilon
        val res = Speeds()
        res.setFieldCentric(Pose(vectorResult, headingResult.first))
        return res
    }
}
