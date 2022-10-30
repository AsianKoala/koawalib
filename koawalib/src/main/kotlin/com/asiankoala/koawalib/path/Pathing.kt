package com.asiankoala.koawalib.path

import com.asiankoala.koawalib.math.*
import com.asiankoala.koawalib.util.Alliance
import org.ejml.simple.SimpleMatrix
import kotlin.math.*

/*
sources i used to create my path generation system:
-------------------------------------------------

*************************THE GOAT PAPER ****************************
https://people.cs.clemson.edu/~dhouse/courses/405/notes/splines.pdf
this paper basically wrote the entirety of my path generation system
********************************************************************

https://pomax.github.io/bezierinfo/#arclength (amazing primer on bezier curves, which can be applied to splines)
https://pomax.github.io/bezierinfo/legendre-gauss.html (for my gaussian-legendre weights)
https://www.wolframalpha.com/input?i=row+reduce+%5B%5B0%2C0%2C0%2C0%2C0%2Cf%2Cu%5D%2C%5B0%2C0%2C0%2C0%2Ce%2C0%2Cv%5D%2C%5B0%2C0%2C0%2C2d%2C0%2C0%2Cw%5D%2C%5Ba%2Cb%2Cc%2Cd%2Ce%2Cf%2Cx%5D%2C%5B5a%2C4b%2C3c%2C2d%2C1e%2C0%2Cy%5D%2C%5B20a%2C12b%2C6c%2C2d%2C0%2C0%2Cz%5D%5D
the above link is what i used to generate splines themselves, as well as the first link
https://www.youtube.com/watch?v=unWguclP-Ds&list=PLC8FC40C714F5E60F&index=2
great channel on numerically computing anything (integrals, derivatives, etc.)
https://github.com/GrappleRobotics/Pathfinder/tree/master/Pathfinder/src/include/grpl/pf/path
this is the basis of my arc approximation
of course, credit given to rr for the idea of using splines for paths, as well as its projection algorithm
 */

class Polynomial(coeffVec: SimpleMatrix) {
    val coeffs = MutableList(coeffVec.numElements, init = { index -> coeffVec[index] })
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
    val ref: Vector
    val length: Double
    val angleOffset: Double
    var curvature: Double; private set
    var dt = 0.0; private set
    var tStart = 0.0; private set
    private var curvatureSet = false
    private var dkds = 0.0
    private var tEnd = 0.0

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

    fun getCorrectCurvature(s: Double): Double = curvature + s * dkds
    fun linearlyInterpolate(s: Double) = ref + (end - start) * (s / length)
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
            // todo: check if this works
            val sNN = start.normSq
            val mNN = mid.normSq
            val eNN = end.normSq
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
// s(t) = int 0->t |r'(u)| du
// s(t) = int 0-> sqrt((dx/du)^2 + (dy/du)^2) du
// s'(t) = |r'(t)|
// ok that's pretty chill, now lets take another deriv
// first have to expand out |r'(t)|
// s'(t) = |r'(t)|
// = sqrt(x'(t)^2 + y'(t)^2)
// s''(t) = d/dt (x'(t)^2 + y'(t)^2)^(1/2)
// = (x'(t)^2 + y'(t)^2)^(-1/2) * d/dt [x'(t)^2 + y'(t)^2]
// = (x'(t)^2 + y'(t)^2)^(-1/2) * 2 * x'(t) * x''(t) + 2 * y'(t) * y''(t)
// = (x'(t)^2 + y'(t)^2)^(-1/2) * (2 * ((x'(t) * x''(t) + y'(t) * y''(t))
// = (2 * ((x'(t) * x''(t) + y'(t) * y''(t)))) / sqrt(x'(t)^2 + y'(t)^2
// this is pretty much unusable in it's current form, so lets just convert it back to vectors
// now that we've finished differentiation
// s''(t) = (2 * r'(t) dot r''(t)') / |r'(t)|
// now the rest is pretty obvious from here..
// to find t'(s) and t''(s), just use inverse function theorem
// t'(s) = 1 / s'(t)
// t''(s) is a bit more complicated but lets just solve for it here
// t(s(t)) = t
// t'(s(t)) * s'(t) = 1 (obviouly can see previous thing from here)
// t''(s(t)) * s'(t) * s'(t) + s''(t) * t'(s(t)) = 0
// t''(s(t)) * s'(t)^2 + s''(t) * t'(s(t)) = 0
// t''(s(t)) * s'(t)^2 + s''(t) / s'(t) = 0
// t''(s(t)) = -s''(t) / s'(t)^3
// and there we go
// in summary, these are the equations we need:
// s'(t) = |r'(t)|
// s''(t) = (2 * r'(t) dot r''(t)') / |r'(t)|
// t'(s) = 1 / s'(t)
// t''(s(t)) = -s''(t) / s'(t)^3
// r(s) = r(t(s))
// r'(s) = r'(t(s)) * t'(s) = r'(t) / |r'(t)|
// r''(s) = r''(t(s)) * t'(s)^2 + r'(t(s)) * t''(s)
// did i really just type all of this out instead of
// doing it on paper? yes. am i lazy? yes.
interface SmoothCurve {
    val x: Polynomial
    val y: Polynomial
    val length: Double
    fun invArc(s: Double): Double

    fun rt(t: Double, n: Int = 0) = Vector(x[t, n], y[t, n])

    private fun dsdt(t: Double, n: Int = 1): Double {
        return when (n) {
            1 -> rt(t, 1).norm
            2 -> (2 * (rt(t, 1) dot rt(t, 2))) / dsdt(t)
            else -> throw Exception("im not implementing any more derivatives")
        }
    }

    private fun dtds(t: Double, n: Int = 1): Double {
        return when (n) {
            1 -> 1.0 / dsdt(t)
            2 -> -dsdt(t, 2) / dsdt(t).pow(3)
            else -> throw Exception("im not implementing any more derivatives")
        }
    }

    private fun rs(s: Double, n: Int = 0): Vector {
        val t = invArc(s)
        return when (n) {
            0 -> rt(t)
            1 -> rt(t, 1).unit
            2 -> rt(t, 2) * dtds(t).pow(2) + rt(t, 1) * dtds(t, 2)
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

interface PiecewiseSplineInterpolator {
    fun interpolate()
    val piecewiseCurve: MutableList<Spline>
    val length: Double
    operator fun get(s: Double, n: Int): Pose
}

data class HermiteControlVector1d(
    val zero: Double = 0.0,
    val first: Double = 0.0,
) {
    private val derivatives = listOf(zero, first)
    operator fun get(n: Int) = derivatives[n]
}

class HermiteControlVector2d(zero: Vector, first: Vector) {
    val x: HermiteControlVector1d
    val y: HermiteControlVector1d

    init {
        x = HermiteControlVector1d(zero.x, first.x)
        y = HermiteControlVector1d(zero.y, first.y)
    }
}

val CUBIC_HERMITE_MATRIX = SimpleMatrix(
    4, 4, true,
    doubleArrayOf(
        0.0, 0.0, 0.0, 1.0,
        0.0, 0.0, 1.0, 0.0,
        1.0, 1.0, 1.0, 1.0,
        3.0, 2.0, 1.0, 0.0
    )
)

fun interface HeadingController {
    val flip get() = HeadingController { (update(it) + 180.0.radians).angleWrap }
    fun update(t: Vector): Double
}

val DEFAULT_HEADING_CONTROLLER = HeadingController { it.angle }
val FLIPPED_HEADING_CONTROLLER = DEFAULT_HEADING_CONTROLLER.flip

// headingFunction inputs are (spline, s (into spline), n)
class HermiteSplineInterpolator(
    private val headingController: HeadingController,
    private vararg val controlPoses: Pose,
) : PiecewiseSplineInterpolator {
    private var _length = 0.0
    private val arcLengthSteps = mutableListOf<Double>()
    override val piecewiseCurve = mutableListOf<Spline>()
    override val length: Double get() = _length

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

    override operator fun get(s: Double, n: Int): Pose {
        val cs = clamp(s, 0.0 + EPSILON, length - EPSILON)
        arcLengthSteps.forEachIndexed { i, x ->
            if (x + piecewiseCurve[i].length > cs) {
                val v = piecewiseCurve[i][cs - x, n]
                val h = headingController.update(piecewiseCurve[i][cs - x, 1])
                return Pose(v, h)
            }
        }

        throw Exception("we fucked up")
    }
}

open class Path(val interpolator: PiecewiseSplineInterpolator) {
    val start get() = this[0.0]
    val end get() = this[length]
    val length get() = interpolator.length

    operator fun get(s: Double, n: Int = 0) = interpolator[s, n]

    // yoinked this from rr
    fun project(p: Vector, pGuess: Double = length / 2.0) = (1..10).fold(pGuess) { s, _ ->
        clamp(s + ((p - this[s].vec) dot this[s, 1].vec), 0.0, length)
    }

    init {
        interpolator.interpolate()
    }
}

class HermitePath(
    headingController: HeadingController,
    private vararg val controlPoses: Pose
) : Path(HermiteSplineInterpolator(headingController, *controlPoses)) {
    fun map(hc: HeadingController, flipFunc: (Pose) -> Pose): HermitePath {
        return HermitePath(
            hc,
            *controlPoses.map(flipFunc).toTypedArray()
        )
    }
}
