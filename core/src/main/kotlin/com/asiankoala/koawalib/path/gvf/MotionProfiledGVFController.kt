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
    private var error = 0.0
    private var headingError = 0.0

    override val isFinished: Boolean
        get() = path.length - s < epsilon &&
            pose.vec.dist(path.end.vec) < epsilon &&
            headingError.absoluteValue < thetaEpsilon

    private fun calcGVF(): Vector {
        val tangent = path[s, 1].vec
        normal = tangent.rotate(PI / 2.0)
        val displacementVec = path[s].vec - pose.vec
        error = displacementVec.norm * (displacementVec cross tangent).sign
        Logger.logInfo("s: $s, d: $tangent, r: $displacementVec, e: $error")
        return tangent - normal * kN * errorMap.invoke(error)
    }

    // basic feedforward
    private fun feedforwardMap(velAccel: Pair<Double, Double>) =
        kStatic * velAccel.first.sign + velAccel.first * kV + velAccel.second * kA

    // the vector triple product BAC-CAB identity
    private fun tripleProduct(a: Vector, b: Vector, c: Vector) = b * (a dot c) - c * (a dot b)

    // lol i'm lazy af. just a simple p controller our target heading
    // might be enough? to fix tho i would have to rework how my pathing system
    // deals with heading. just needs a bit of testing tbh
    private fun headingControl(): Pair<Double, Double> {
        headingError = (path[s].heading - pose.heading).angleWrap.degrees
        return Pair(error / kOmega * state.v, error)
    }

    // d/ds v(p(s)) = v'(p(s)) * p'(s)
    // d^2/ds^2 = v''(p(s)) * p'(s) * p'(s) + v(p(s)) * p''(s)
    // now we need to differentiate our gvf
    // v'(s) = t' - kn * (n' * e + e' * n)
    // bc im lazy lets just assume linear error map
    // so e' = 1.0
    // v' = t' - k_n * (n' * e + n)
    private fun vectorControl(): Pair<Vector, Vector> {
        val v = calcGVF()
        val md = v.unit
        Logger.addTelemetryData("md", md)
        val vel = md * state.v
        Logger.addTelemetryData("vel", vel)
        val dvds = path[s, 2].vec - (path[s, 2].vec.rotate(PI / 2.0) * error + normal) * kN
        // from https://math.stackexchange.com/questions/2983445/unit-vector-differentiation
        val mdDot = tripleProduct(v, dvds, v) / v.norm.pow(3)
        val accel = mdDot * state.v * state.v + md * state.a
        return Pair(vel, accel)
    }

    private fun calcFeedforward(vel: Pose, accel: Pose): Pose {
        val vels = KMecanumDrive.mecKinematics(vel)
        Logger.addTelemetryData("vels", vels)
        val accels = KMecanumDrive.mecKinematics(accel)
        val powers = vels.zip(accels).map(::feedforwardMap) // kotlin syntax so clean
        return Speeds()
            .apply { setWheels(powers, pose.heading) }
            .getRobotCentric(pose.heading)
    }

    override fun update(currPose: Pose): Pose {
        pose = currPose
        s = path.project(pose.vec, s)
        state = profile[s]
        if (kIsTuning) Logger.addVar("target velocity", state.v)
        val headingResult = headingControl()
        val vectorResult = vectorControl()
        val vel = Pose(vectorResult.first, headingResult.first)
        val accel = Pose(vectorResult.second, 0.0)
        return calcFeedforward(vel, accel)
    }
}
