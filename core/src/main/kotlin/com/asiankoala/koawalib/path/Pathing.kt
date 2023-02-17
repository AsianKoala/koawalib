package com.asiankoala.koawalib.path

import com.acmerobotics.dashboard.canvas.Canvas
import com.asiankoala.koawalib.command.commands.Cmd
import com.asiankoala.koawalib.control.controller.PIDFController
import com.asiankoala.koawalib.control.controller.PIDGains
import com.asiankoala.koawalib.math.*
import com.asiankoala.koawalib.subsystem.drive.KMecanumOdoDrive
import com.qualcomm.robotcore.util.ElapsedTime
import org.ejml.simple.SimpleMatrix
import kotlin.math.*

class Polynomial(coeffVec: SimpleMatrix) {
    private val coeffs = MutableList(coeffVec.numElements, init = { index -> coeffVec[index] })
    private val degree by lazy { coeffs.size - 1 }

    /**
     * yes this is totally unreadable i know
     * but it's cool so whatever
     * @param t function input
     * @param n nth deriv
     */
    operator fun get(t: Double, n: Int = 0): Double {
        return coeffs.foldIndexed(0.0) { i, acc, c ->
            acc + (0 until n).fold(1.0) { a, x ->
                (degree - i - x) * a
            } * c * t.pow(max(0, degree - i - n))
        }
    }

    override fun toString(): String {
        return coeffs.subList(1, degree)
            .foldIndexed("${coeffs[0]}t^$degree ") { i, acc, c ->
                acc + "+ ${c}t^${degree - i - 1} "
            } + "+ ${coeffs.last()}"
    }

    init {
        require(coeffVec.isVector)
    }
}

// To actually create paths, we can't have splines parametrized on [0,1] with t,
// instead splines have to be parametrized according to arc length s
// This problem is obvious when combining splines, cause t is completely
// arbitrary in different parts of a path (e.g.) for the first spline,
// t=1 might correspond to 10 inches of arc length, while t=1 at the
// another spline segment might correspond to 20 inches.
// Converting from t -> s is pretty simple itself (remember multi) but we want s -> t in t(s)
// ryan talks about this in his spline paper
// see https://github.com/GrappleRobotics/Pathfinder
// this arc class is used to parametrize, pulled from above link ^
class Arc(
    private val start: Vector,
    mid: Vector,
    private val end: Vector
) {
    private val ref: Vector
    private val angleOffset: Double
    private var curvatureSet = false
    private var dkds = 0.0
    private var tEnd = 0.0
    val length: Double
    var curvature: Double; private set
    var dt = 0.0; private set
    var tStart = 0.0; private set

    private fun linearlyInterpolate(s: Double) = ref + (end - start) * (s / length)

    fun setCurvature(startK: Double, endK: Double) {
        curvature = startK
        dkds = (endK - startK) / length
        curvatureSet = true
    }

    fun setT(tPair: Pair<Double, Double>) {
        tStart = tPair.first
        tEnd = tPair.second
        dt = tEnd - tStart
    }

    fun interpolateSAlongT(s: Double) = tStart + dt * (s / length)

    fun get(s: Double): Vector {
        return if (curvature != 0.0) {
            ref + Vector.fromPolar(1.0 / curvature, angleOffset + (s * curvature))
        } else {
            linearlyInterpolate(s)
        }
    }

    init {
        val coeffMatrix = SimpleMatrix(
            2, 2, true,
            doubleArrayOf(
                2 * (start.x - end.x), 2 * (start.y - end.y),
                2 * (start.x - mid.x), 2 * (start.y - mid.y)
            )
        )

        val det = coeffMatrix.determinant()

        if (det == 0.0) {
            curvature = 0.0
            ref = start
            val delta = end - start
            length = delta.norm
            angleOffset = atan2(delta.y, delta.x)
        } else {
            val sNN = start.norm * start.norm
            val mNN = mid.norm * mid.norm
            val eNN = end.norm * end.norm
            val r = SimpleMatrix(
                2, 1, true,
                doubleArrayOf(
                    sNN - eNN,
                    sNN - mNN
                )
            )
            val inverse = coeffMatrix.invert()
            val product = inverse.mult(r)
            ref = Vector(product[0], product[1])
            angleOffset = (start - ref).angle
            val angle1 = (end - ref).angle
            curvature = 1.0 / (start - ref).norm
            length = (angle1 - angleOffset).absoluteValue / curvature
            if (angle1 < angleOffset) curvature *= -1
        }
    }
}

// r(s) = r(t(s))
// r'(s) = r'(t(s)) * t'(s)
// r''(s) = r''(t(s)) * t'(s) * t'(s) + r'(t(s)) * t''(s)
// r''(s) = r''(t(s)) * t'(s)^2 + r'(t(s)) * t''(s)
// s(t) = 0 -> t int |r'(u)| du
// s' = ||r'||
// now to find s'', we know d/dt ||v|| = (v dot v') / |v|
// s'' = (r' dot r'') / |r'|
// now the rest is pretty obvious from here..
// to find t'(s) and t''(s), just use inverse function theorem
// t'(s) = 1 / s'(t)
// t''(s) is a bit more complicated but lets just solve for it here
// t(s(t)) = t
// t'(s(t)) * s'(t) = 1 (obviouly can see previous thing from here)
// t'' * s' * s' + s'' * t' = 0
// t'' * s'^2 + s'' * t' = 0
// t'' * s'^2 + s'' / s' = 0
// t'' = -s'' / s'^3
// and there we go
// in summary, these are the equations we need:
// s' = ||r'||
// s'' = (r' dot r'') / |r'|
// t' = 1 / s'(t)
// t'' = -s''(t) / s'(t)^3
// r = r(t)
// r' = r' / |r'(t)|
// r'' = r'' * t'(s)^2 + r'(t(s)) * t''(s)
// update november 09 2022: my old self is stupid as fuck
interface SmoothCurve {
    val x: Polynomial
    val y: Polynomial
    val length: Double
    fun invArc(s: Double): Double

    fun rt(t: Double, n: Int = 0) = Vector(x[t, n], y[t, n])

    private fun dsdt(t: Double, n: Int = 1): Double {
        return when (n) {
            1 -> rt(t, 1).norm
            2 -> (rt(t, 1) dot rt(t, 2)) / rt(t, 1).norm
            3 -> {
                val num = rt(t, 1) dot rt(t, 2)
                val numDeriv = rt(t, 2).norm.pow(2) + (rt(t, 1) dot rt(t, 2))
                val denom = rt(t, 1).norm
                val denomDeriv = dsdt(t, 2)
                (numDeriv * denom - num * denomDeriv) / denomDeriv.pow(3)
            }
            else -> throw Exception("im not implementing any more derivatives")
        }
    }

    private fun dtds(t: Double, n: Int = 1): Double {
        return when (n) {
            1 -> 1.0 / dsdt(t)
            2 -> -dsdt(t, 2) / dsdt(t).pow(3)
            3 -> (3 * dsdt(t, 2).pow(2) - dsdt(t, 3) * dsdt(t)) / dsdt(t).pow(4)
            else -> throw Exception("im not implementing any more derivatives")
        }
    }

    private fun rs(s: Double, n: Int = 0): Vector {
        val t = invArc(s)
        return when (n) {
            0 -> rt(t)
            1 -> rt(t, 1).unit
            2 -> rt(t, 2) * dtds(t).pow(2) + rt(t, 1) * dtds(t, 2)
            3 -> rt(t, 1) * dtds(t, 3) + (rt(t, 3) * dtds(t).pow(2) + rt(t, 2) * 3.0 * dtds(t, 2))
            else -> throw Exception("im not implementing any more derivatives")
        }
    }

    operator fun get(s: Double, n: Int = 0): Vector {
        return when (n) {
            0 -> rs(s)
            1 -> rs(s, 1)
            2 -> rs(s, 2)
            else -> throw Exception("im not implementing any more derivatives")
        }
    }
}

class Spline(
    override val x: Polynomial,
    override val y: Polynomial
) : SmoothCurve {
    private var _length = 0.0
    private val arcs = mutableListOf<Arc>()
    override val length: Double get() = _length

    // now that we have our spline parametrized into arcs,
    // we can find the corresponding t with s by iterating across
    // our arc array and finding what iteration it is at
    // ok this is a shitty name but like... is it really?
    override fun invArc(s: Double): Double {
        if (s <= 0) return 0.0
        if (s >= length) return 1.0
        arcs.fold(0.0) { acc, arc ->
            if (acc + arc.length > s) return arc.interpolateSAlongT(acc - s)
            acc + arc.length
        }
        throw Exception("i think ur pretty bad a coding neil")
    }

    override fun toString(): String {
        return x.toString() + "\n" + y.toString()
    }

    init {
        val tPairs = ArrayDeque<Pair<Double, Double>>()
        tPairs.add(Pair(0.0, 1.0))
        var its = 0
        while (tPairs.isNotEmpty()) {
            val curr = tPairs.removeFirst()
            val midT = (curr.first + curr.second) / 2.0

            val startV = rt(curr.first)
            val endV = rt(curr.second)
            val midV = rt(midT)

            // from multi: k = (a x v) / |v|^3
            val startK = rt(curr.first, 2).cross(rt(curr.first, 1)) / rt(curr.first, 1).norm.pow(3)
            val endK = rt(curr.second, 2).cross(rt(curr.second, 1)) / rt(curr.second, 1).norm.pow(3)
            val arc = Arc(startV, midV, endV)
            // we want to make our approximations as circle-y as possible, so
            // the arc approximation will be more accurate
            // a more accurate interpolation when param from s -> t
            // might want to try adjusting 0.01 for curve or 0.5 for arc length later
            // update 10/09/22: it seems limiting curvature works a lot better than length
            val subdivide = (endK - startK).absoluteValue > 0.01 || arc.length > 0.5
            if (subdivide) {
                tPairs.add(Pair(midT, curr.second))
                tPairs.add(Pair(curr.first, midT))
            } else {
                arc.setCurvature(startK, endK)
                arc.setT(curr)
                arcs.add(arc)
                _length += arc.length
                its++
            }
        }

        // we want to make sure that t is always increasing in this set (array)
        // therefore it will be possible to interpolate when invArc
        arcs.sortBy { it.tStart }
    }
}

fun interface HeadingController {
    operator fun get(spline: Spline, s: Double, n: Int): Double
    fun flip() = HeadingController { spline, s, n -> (this[spline, s, n] + PI).angleWrap }
}

class TangentHeadingController : HeadingController {
    override fun get(spline: Spline, s: Double, n: Int) = when (n) {
        0 -> spline[s].angle
        1 -> spline[s, 1] cross spline[s, 2]
        2 -> spline[s, 1] cross spline[s, 3]
        else -> 0.0
    }
}

class ConstantHeadingController(private val heading: Double) : HeadingController {
    override fun get(spline: Spline, s: Double, n: Int) = when (n) {
        0 -> heading
        else -> 0.0
    }
}

interface PathInterpolator {
    fun interpolate()
}

// headingFunction inputs are (spline, s (into spline), n)
class HermiteSplineInterpolator(
    private val headingController: HeadingController,
    private val controlPoses: Array<out Pose>
) : PathInterpolator {
    class HermiteControlVector2d(zero: Vector, first: Vector) {
        data class HermiteControlVector1d(
            val zero: Double = 0.0,
            val first: Double = 0.0,
        ) {
            private val derivatives = listOf(zero, first)
            operator fun get(n: Int) = derivatives[n]
        }
        val x = HermiteControlVector1d(zero.x, first.x)
        val y = HermiteControlVector1d(zero.y, first.y)
    }
    private var _length = 0.0
    private val arcLengthSteps = mutableListOf<Double>()
    private val piecewiseCurve = mutableListOf<Spline>()
    val length: Double get() = _length

    private fun fitSplineToControlVectors(
        start: HermiteControlVector2d,
        end: HermiteControlVector2d
    ): Spline {
        val M = CUBIC_HERMITE_MATRIX.solve(
            SimpleMatrix(
                4, 2, true,
                doubleArrayOf(
                    start.x.zero, start.y.zero,
                    start.x.first, start.y.first,
                    end.x.zero, end.y.zero,
                    end.x.first, end.y.first
                )
            )
        )

        val x = M.extractVector(false, 0)
        val y = M.extractVector(false, 1)

        return Spline(Polynomial(x), Polynomial(y))
    }

    override fun interpolate() {
        var curr = controlPoses[0]
        for (target in controlPoses.slice(1 until controlPoses.size)) {
            val r = curr.vec.dist(target.vec)
            val s = HermiteControlVector2d(curr.vec, Vector.fromPolar(r, curr.heading))
            val e = HermiteControlVector2d(target.vec, Vector.fromPolar(r, target.heading))
            val curve = fitSplineToControlVectors(s, e)
            piecewiseCurve.add(curve)
            arcLengthSteps.add(_length)
            _length += curve.length
            curr = target
        }
    }

    operator fun get(s: Double, n: Int = 0): Pose {
        val cs = clamp(s, 0.0 + EPSILON, length - EPSILON)
        arcLengthSteps.forEachIndexed { i, x ->
            if (x + piecewiseCurve[i].length > cs) {
                val arcl = cs - x
                val spline = piecewiseCurve[i]
                val vec = spline[arcl, n]
                val h = headingController[spline, arcl, n]
                return Pose(vec, h)
            }
        }

        throw Exception("we fucked up")
    }

    init {
        interpolate()
    }

    companion object {
        private val CUBIC_HERMITE_MATRIX = SimpleMatrix(
            4, 4, true,
            doubleArrayOf(
                0.0, 0.0, 0.0, 1.0,
                0.0, 0.0, 1.0, 0.0,
                1.0, 1.0, 1.0, 1.0,
                3.0, 2.0, 1.0, 0.0
            )
        )
    }
}

interface Drawable {
    fun draw(t: Canvas): Canvas
}

open class HermitePath(headingController: HeadingController, vararg controlPoses: Pose) {
    private val interpolator = HermiteSplineInterpolator(headingController, controlPoses)
    val start get() = this[0.0]
    val end get() = this[length]
    val length get() = interpolator.length
    val sampled by lazy { PathDrawer(this) }

    open operator fun get(s: Double, n: Int = 0) = interpolator[s, n]

    // yoinked this from rr
    fun project(p: Vector, pGuess: Double = length / 2.0) = (1..10).fold(pGuess) { s, _ ->
        clamp(s + ((p - this[s].vec) dot this[s, 1].vec), 0.0, length)
    }
}

class PathDrawer(path: HermitePath) : Drawable {
    private val steps = 50
    private val xPoints = DoubleArray(50)
    private val yPoints = DoubleArray(50)

    override fun draw(t: Canvas): Canvas {
        return t.setStroke("red").setStrokeWidth(1).strokePolyline(xPoints, yPoints)
    }

    init {
        for (i in 0 until steps) {
            val vec = path[i * path.length].vec
            xPoints[i] = vec.x
            yPoints[i] = vec.y
        }
    }
}

class TangentPath(vararg controlPoses: Pose) : HermitePath(TangentHeadingController(), *controlPoses)
class ConstantHeadingPath(private val heading: Double, vararg controlPoses: Pose) : HermitePath(
    ConstantHeadingController(heading), *controlPoses
)

data class ProjQuery(val cmd: Cmd, val t: Double)

open class Waypoint(
    var vec: Vector,
    val follow: Double,
    val cmd: Cmd? = null
) {
    val x = vec.x
    val y = vec.y
    open fun flip() = Waypoint(vec.copy(y = -vec.y), follow, cmd)
}

open class HeadingControlledWaypoint(
    vec: Vector,
    rad: Double,
    val h: Double,
    cmd: Cmd? = null
) : Waypoint(vec, rad, cmd) {
    override fun flip() = HeadingControlledWaypoint(vec.copy(y = -vec.y), follow, (h + PI).angleWrap, cmd)
}

class StopWaypoint(
    vec: Vector,
    rad: Double,
    h: Double,
    val epsilon: Double,
    val thetaEpsilon: Double,
    cmd: Cmd?
) : HeadingControlledWaypoint(vec, rad, h, cmd) {
    override fun flip() = StopWaypoint(vec.copy(y = -vec.y), follow, (h + PI).angleWrap, epsilon, thetaEpsilon, cmd)
}

// 8802's pp impl
class PurePursuitPath(
    private val drive: KMecanumOdoDrive,
    private var waypoints: List<Waypoint>,
    transGains: PIDGains,
    rotGains: PIDGains,
    private val closeExponent: Double = 1.0 / 6.0,
    private val undershootDistance: Double = 6.0,
    private val switchDistance: Double = 18.0
) {
    private var index = -1
    private var skip = true
    private val timer = ElapsedTime()
    private val timeout = 2000.0
    private val xController = PIDFController(transGains)
    private val yController = PIDFController(transGains)
    private val rController = PIDFController(rotGains).apply {
        setInputBounds(-PI, PI)
    }
    val isFinished get() = index >= waypoints.size - 1

    private fun setTarget(controller: PIDFController, curr: Double, target: Double): Double {
        controller.targetPosition = target
        return controller.update(curr)
    }

    private fun zoom(tx: Double, ty: Double, th: Double): Pose {
        val trans = Vector(
            setTarget(xController, drive.pose.x, tx),
            setTarget(yController, drive.pose.y, ty),
        )
        val rot = setTarget(rController, drive.pose.heading, th)
        return Pose(
            trans.rotate(PI / 2.0 - drive.pose.heading),
            rot
        )
    }

    private fun goToPosition(target: Waypoint, end: StopWaypoint?) {
        val delta = drive.pose.vec.dist(target.vec)
        drive.setPowers(
            if (end == null || delta < switchDistance) {
                zoom(
                    target.vec.x,
                    target.vec.y,
                    if (target is HeadingControlledWaypoint) target.h else (target.vec - drive.pose.vec).angle
                )
            } else if (delta > undershootDistance) { // need to incporporate robot velocity later.
                val t = lineCircleIntersection(end.vec, drive.pose.vec, end.vec, undershootDistance)
                zoom(t.x, t.y, end.h)
            } else {
                listOf(
                    target.vec.x - drive.pose.x,
                    target.vec.y - drive.pose.y,
                    (end.h - drive.pose.heading).angleWrap
                ).map {
                    abs(it).pow(closeExponent) * sign(it)
                }.let {
                    Pose(Vector(it[0], it[1]).rotate(PI / 2.0 - drive.pose.heading), it[2])
                }
            }
        )
    }

    private fun project(v: Vector, onto: Vector): Vector {
        return onto * ((v dot onto) / (onto dot onto))
    }

    private fun curveToSegment(start: Waypoint, end: Waypoint) {
        val proj = project(drive.pose.vec, end.vec - start.vec)
        val inter = lineCircleIntersection(proj, start.vec, end.vec, end.follow)
        end.vec = inter
        goToPosition(end, if (end is StopWaypoint) end else null)
    }

    fun flip() {
        waypoints = waypoints.map(Waypoint::flip)
    }

    fun update() {
        if (skip) {
            index++
            waypoints[0].cmd?.schedule()
        }

        val target = waypoints[index + 1]

        skip = when {
            target is StopWaypoint && timer.milliseconds() > timeout -> true
            target is StopWaypoint && drive.pose.vec.dist(target.vec) < target.epsilon &&
                (drive.pose.heading - target.h).angleWrap < target.thetaEpsilon -> true
            drive.pose.vec.dist(target.vec) < target.follow -> true
            else -> {
                if (drive.vel.vec.norm > 1.0) timer.reset()
                false
            }
        }


        if (isFinished) return

        if (target is StopWaypoint && drive.pose.vec.dist(target.vec) < target.follow) {
            goToPosition(target, target)
        } else {
            curveToSegment(waypoints[index], target)
        }
    }
}
