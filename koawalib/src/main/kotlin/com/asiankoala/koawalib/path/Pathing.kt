package com.asiankoala.koawalib.path

import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.clamp
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.pow
import org.ejml.simple.SimpleMatrix

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
data class DifferentiablePoint(
    val zero: Double = 0.0,
    val first: Double = 0.0,
    val second: Double = 0.0
)

class DifferentiablePoint2d(zero: Vector, first: Vector) {
    val x: DifferentiablePoint
    val y: DifferentiablePoint

    init {
        x = DifferentiablePoint(zero.x, first.x)
        y = DifferentiablePoint(zero.y, first.y)
    }
}

abstract class DifferentiableFunction {
    abstract operator fun get(t: Double): DifferentiablePoint
}


// in the form ax^3 + bx^2 + cx + d
// created from specifying the start point and derivative
// https://cdn.discordapp.com/attachments/770810258322227231/1028432435236581386/unknown.png
// enforces c^2 because i force curvature at begin/end of spline to be 0.
// thus we don't have that extra degree of freedom that quintics have,
// but imo it doesn't matter that much
class Cubic(
    start: DifferentiablePoint,
    end: DifferentiablePoint
) : DifferentiableFunction() {
    private val coeffVec: List<Double> 

    override operator fun get(t: Double): DifferentiablePoint {
        return DifferentiablePoint(
            coeffVec[0] * t.pow(3) + coeffVec[1] * t.pow(2) + coeffVec[2] * t + coeffVec[3],
            3 * coeffVec[0] * t.pow(2) + 2 * coeffVec[1] * t + coeffVec[2],
            3 * coeffVec[0] * t + 2 * coeffVec[1]
        )
    }

    override fun toString(): String {
        return "${coeffVec[0]}t^3 + ${coeffVec[1]}^2 + ${coeffVec[2]}t + ${coeffVec[3]}"
    }

    init {
        val A = SimpleMatrix(4, 4, true, doubleArrayOf(
            0.0, 0.0, 0.0, 1.0,
            0.0, 0.0, 1.0, 0.0,
            1.0, 1.0, 1.0, 1.0,
            3.0, 2.0, 1.0, 0.0
        ))

        val B = SimpleMatrix(4, 1, true, doubleArrayOf(
            start.zero,
            start.first,
            end.zero,
            end.first
        ))

        val x = A.solve(B)
        coeffVec = listOf(
            x[0],
            x[1],
            x[2],
            x[3]
        )
    }
}

// in the form ax^5 + bx^4 + cx^3 + dx^2 + ex + f
class Quintic(
    start: DifferentiablePoint,
    end: DifferentiablePoint
) : DifferentiableFunction() {
    private val coeffVec: List<Double>

    override operator fun get(t: Double): DifferentiablePoint {
        return DifferentiablePoint(
            coeffVec[0] * t.pow(5) + coeffVec[1] * t.pow(4) + coeffVec[2] * t.pow(3) +
                    coeffVec[3] * t.pow(2) + coeffVec[4] * t + coeffVec[5],
            5 * coeffVec[0] * t.pow(4) + 4 * coeffVec[1] * t.pow(3) + 3 * coeffVec[2] * t.pow(2) +
                    2 * coeffVec[3] * t + coeffVec[4],
            20 * coeffVec[0] * t.pow(3) + 12 * coeffVec[1] * t.pow(2) + 6 * coeffVec[2] * t + 2 * coeffVec[3]
        )
    }

    override fun toString(): String {
        return "${coeffVec[0]}t^5 + ${coeffVec[1]}^4 + ${coeffVec[2]}t^3 + ${coeffVec[3]}t^2 + ${coeffVec[4]}t + ${coeffVec[5]}"
    }

    init {
        val A = SimpleMatrix(6, 6, true, doubleArrayOf(
            0.0, 0.0, 0.0, 0.0, 0.0, 1.0,
            0.0, 0.0, 0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, 0.0, 2.0, 0.0, 0.0,
            1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
            5.0, 4.0, 3.0, 2.0, 1.0, 0.0,
            20.0, 12.0, 6.0, 2.0, 0.0, 0.0
        ))

        val B = SimpleMatrix(6, 1, true, doubleArrayOf(
            start.zero,
            start.first,
            0.0,
            end.zero,
            end.first,
            0.0
        ))

        val x = A.solve(B)
        coeffVec = listOf(
            x[0],
            x[1],
            x[2],
            x[3],
            x[4],
            x[5]
        )
    }
}

/*
see https://www.youtube.com/watch?v=unWguclP-Ds&list=PLC8FC40C714F5E60F&index=2
and https://pomax.github.io/bezierinfo/#arclength
arc length is int 0->1 ||r'(t)|| dt of course
Gaussian quadrature is derived on the interval [-1,1]
but changing it to [0,1] is arbitrarily easy
 */
data class GaussianLegendreCoefficients(
    val weight: Double,
    val abscissa: Double
)

abstract class GaussianQuadratureTable {
    abstract val list: List<GaussianLegendreCoefficients>
}

class FivePointGaussianLegendre : GaussianQuadratureTable() {
    override val list: List<GaussianLegendreCoefficients>
        get() = listOf(
            GaussianLegendreCoefficients(0.5688888888888889, 0.0000000000000000),
            GaussianLegendreCoefficients(0.4786286704993665, -0.5384693101056831),
            GaussianLegendreCoefficients(0.4786286704993665, 0.5384693101056831),
            GaussianLegendreCoefficients(0.2369268850561891, -0.9061798459386640),
            GaussianLegendreCoefficients(0.2369268850561891, 0.9061798459386640)
        )
}

class GaussianQuadrature(
    curve: Spline,
    table: GaussianQuadratureTable
) {
    var length = 0.0
        private set

    init {
        for(coefficient in table.list) {
            val t = 0.5 * (1.0 + coefficient.abscissa) // used for converting from [-1,1] to [0,1]
            length += curve.rt(t, 1).norm * coefficient.weight
        }
        length *= 0.5
    }
}

// To actually create paths, we can't have splines parametrized on [0,1] with t,
// instead splines have to be parametrized according to arc length s.
// This problem is obvious when combining splines, cause t is completely
// arbitrary in different parts of a path (e.g.) for the first spline,
// t=1 might correspond to 10 inches of arc length, while t=1 at the
// another spline segment might correspond to 20 inches.
// Parametrizing is pretty simple itself (second week of multi var calc)
// 1. s(t) = int 0->t |r'(u)| du
// 2. t(s) = inverse function of s(t)
// 3. plug t(s) into r(t) (our spline)
// 4. r(t(s)) now is our give parametrized by arc length
// from here on out r((t(s)) will just be referred to as r(s) since its already reparamed
// Ryan (rbrott) talks about this in section 5 of his Quintic Splines for FTC paper
// r(s) obviously has different derivatives now, but they are fairly simple to find
// by just chain ruling
// r(s) = r(t(s))
// r'(s) = r'(t(s)) * t'(s)
// r''(s) = r''(t(s)) * t'(s) * t'(s) + r'(t(s)) * t''(s)
// r''(s) = r''(t(s)) * t'(s)^2 + r'(t(s)) * t''(s)
// s(t) = int 0->t |r'(u)| du
// s(t) = int 0-> sqrt((dx/du)^2 + (dy/du)^2) du
// s'(t) = |r'(t)|
// s'(t) = d/dt int 0->t |r'(u)| du
// = |r'(t)|
// this is cool and all, but we need a way to reparametrize from s->t still
// see https://github.com/GrappleRobotics/Pathfinder/blob/master/Pathfinder/src/include/grpl/pf/path/arc_parameterizer.h
//
// this arc class is used to reparametrize, also pulled from above link ^
// template <typename output_iterator_t>
// size_t parameterize(spline<2> &spline, output_iterator_t &&curve_begin, const size_t max_curve_count,
//                     double t_lo = 0, double t_hi = 1) {
//   _has_overrun = false;
//   if (max_curve_count <= 0) {
//     _has_overrun = true;
//     return 0;
//   }
//
//   double t_mid = (t_hi + t_lo) / 2.0;
//   double k_lo  = spline.curvature(t_lo);
//   double k_hi  = spline.curvature(t_hi);
//
//   augmented_arc2d arc{spline.position(t_lo), spline.position(t_mid), spline.position(t_hi), k_lo, k_hi};
//
//   bool subdivide = (fabs(k_hi - k_lo) > _max_delta_curvature) || (arc.length() > _max_arc_length);
//
//   if (subdivide) {
//     output_iterator_t head = curve_begin;
//
//     size_t len = parameterize(spline, head, max_curve_count, t_lo, t_mid);
//     len += parameterize(spline, head, max_curve_count - len, t_mid, t_hi);
//     return len;
//   } else {
//     arc.set_curvature(k_lo, k_hi);
//     *(curve_begin++) = arc;
//     return 1;
//   }
// }
// really need to make this class cleaner tbh
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
        return if(curvature != 0.0) {
            ref + Vector.fromPolar(1.0 / curvature, angleOffset + (s * curvature))
        } else {
            linearlyInterpolate(s)
        }
    }

    init {
        // ok manually calculating matrix determinants and inverses is hella cringe lmfao
        // should prolly just use apache mat lib instead
        val coeffMatrix = listOf(
            listOf(2 * (start.x - end.x), 2 * (start.y - end.y)),
            listOf(2 * (start.x - mid.x), 2 * (start.y - mid.y))
        )

        val coeffDet = coeffMatrix[0][0] * coeffMatrix[1][1] - coeffMatrix[0][1] * coeffMatrix[1][0]

        if(coeffDet == 0.0) {
            curvature = 0.0
            ref = start
            val delta = end - start
            length = delta.norm
            angleOffset = atan2(delta.y, delta.x)
        } else {
            val sNN = start.normSq
            val mNN = mid.normSq
            val eNN = end.normSq
            val rVec = Vector(sNN - eNN, sNN - mNN)
            val inverse1 = Vector(coeffMatrix[1][1] / coeffDet, -coeffMatrix[0][1] / coeffDet)
            val inverse2 = Vector(-coeffMatrix[1][0] / coeffDet, coeffMatrix[0][0] / coeffDet)
            ref = Vector(inverse1.dot(rVec), inverse2.dot(rVec))
            angleOffset = (start - ref).angle
            val angle1 = (end - ref).angle
            curvature = 1.0 / (start - ref).norm
            length = (angle1 - angleOffset).absoluteValue / curvature
            if(angle1 < angleOffset) curvature *= -1
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
interface DifferentiableCurve {
    val x: DifferentiableFunction
    val y: DifferentiableFunction
    val length: Double
    fun invArc(s: Double): Double

    fun rt(t: Double, n: Int = 0): Vector {
        val xt = x[t]
        val yt = y[t]
        return when(n) {
            1 -> Vector(xt.first, yt.first)
            2 -> Vector(xt.second, yt.second)
            else -> Vector(xt.zero, yt.zero)
        }
    }

    private fun dsdt(t: Double, n: Int = 1): Double {
        return when(n) {
            1 -> rt(t, 1).norm
            2 -> (2 * rt(t, 1).dot(rt(t, 2))) / dsdt(t)
            else -> throw Exception("im not implemented any more derivatives fuck u")
        }
    }

    private fun dtds(t: Double, n: Int = 1): Double {
        return when(n) {
            1 -> 1.0 / dsdt(t)
            2 -> -dsdt(t, 2) / dsdt(t).pow(3)
            else -> throw Exception("im not implemented any more derivatives fuck u")
        }
    }

    private fun rs(s: Double, n: Int = 0): Vector {
        val t = invArc(s)
        println(t)
        return when(n) {
            0 -> rt(t)
            1 -> rt(t, 1).unit
            2 -> rt(t, 2) * dtds(t).pow(2) + rt(t, 1) * dtds(t, 2)
            else -> throw Exception("im not implemented any more derivatives fuck u")
        }
    }

    operator fun get(s: Double, n: Int = 0): Pose {
        return when (n) {
            0 -> Pose(rs(s), rs(s, 1).angle)
            1 -> Pose(rs(s, 1), rs(s, 1).cross(rs(s, 2)))
            2 -> Pose(rs(s, 2), 0.0)
            else -> throw Exception("fuck you im not adding more derivatives :rage:")
        }
    }
}


class Spline(
    override val x: DifferentiableFunction,
    override val y: DifferentiableFunction
) : DifferentiableCurve {
    private var _length = 0.0
    private val arcs = mutableListOf<Arc>()
    override val length: Double get() = _length

    // now that we have our spline parametrized into arcs,
    // we can find the corresponding t with s by iterating across
    // our arc array and finding what iteration it is at
    // ok this is a shitty name but like... is it really?
    override fun invArc(s: Double): Double {
        if(s <= 0) return 0.0
        if(s >= length) return 1.0
        arcs.fold(0.0) { acc, arc ->
            if(acc + arc.length > s) return arc.interpolateSAlongT(acc - s)
            acc + arc.length
        }
        throw Exception("i think ur pretty fucking bad a coding neil")
    }

    override fun toString(): String {
        return x.toString() + "\n" + y.toString()
    }

    init {
        val tPairs = ArrayDeque<Pair<Double, Double>>()
        tPairs.add(Pair(0.0, 1.0))
        var its = 0
        while(tPairs.isNotEmpty()) {
            val curr = tPairs.removeFirst()
            val midT = (curr.first + curr.second) / 2.0

            val startV = rt(curr.first)
            val endV  = rt(curr.second)
            val midV = rt(midT)

            // from multi: k = (a x v) / |v|^3
            val startK = rt(curr.first, 2).cross(rt(curr.first, 1)) / rt(curr.first, 1).norm.pow(3)
            val endK = rt(curr.second, 2).cross(rt(curr.second, 1)) / rt(curr.second, 1).norm.pow(3)
            val arc = Arc(startV, midV, endV)
            // we want to make our approximations as circle-y as possible, so 
            // the arc approximation will be more accurate
            // a more accurate interpolation when param from s -> t
            // might want to try adjusting 0.01 for curve or 1.0 for arc length later
            // update 10/09/22: it seems limiting curvature works a lot better than length
            val subdivide = (endK - startK).absoluteValue > 0.005 || arc.length > 0.1
            if(subdivide) {
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

abstract class Path(poses: List<Pose>) {
    val curveSegments = mutableListOf<DifferentiableCurve>()
    abstract fun project(p: Vector, pGuess: Double): Double
    abstract fun generatePath(poses: List<Pose>)
    abstract val length: Double
    val start by lazy { this[0.0] }
    val end by lazy { this[length] }

    operator fun get(s: Double, n: Int = 0): Pose {
        if(s <= 0.0) return this[0.0, n]
        if(s > length) return this[length - 0.0001, n] // better solution? TODO
        curveSegments.fold(0.0) { acc, spline ->
            if(acc + spline.length > s) {
                return spline[s - acc, n]
            }
            acc + spline.length
        }
        throw Exception("fuck you")
    }

    init {
        generatePath(poses.toList())
    }
}

abstract class SplinePath(
    vararg poses: Pose
) : Path(poses.toList()) {
    private var _length = 0.0
    override val length get() = _length

    abstract fun createSpline(start: DifferentiablePoint2d, end: DifferentiablePoint2d): Spline

    /*
    yoinked this from rr
    basically the way this works is
    take rVec from pose to projection
    this is ideally normal to the curve
    check if its normal with by dot product with tangent vec
    ofc if result is 0, its normal (and therefore the correct projection)
    if not, then add dot product to s
    since dot product literally just finds the amount vec a is parallel to vec b
     */
    override fun project(p: Vector, pGuess: Double) = (1..10).fold(pGuess) { s, _ ->  clamp(s + (p - get(s).vec).dot(get(s, 1).vec), 0.0, length) }

    override fun generatePath(poses: List<Pose>) {
        var curr = poses[0]
        for(target in poses.slice(1 until poses.size)) {
            val cv = curr.vec
            val tv = target.vec
            val r = cv.dist(tv)
            val s = DifferentiablePoint2d(cv, Vector.fromPolar(r, curr.heading))
            val e = DifferentiablePoint2d(tv, Vector.fromPolar(r, target.heading))
            val spline = createSpline(s, e)
            curveSegments.add(spline)
            _length += spline.length
            curr = target
        }
    }
}

class QuinticSplinePath(vararg poses: Pose) : SplinePath(*poses) {
    override fun createSpline(start: DifferentiablePoint2d, end: DifferentiablePoint2d): Spline {
        return Spline(Quintic(start.x, end.x), Quintic(start.y, end.y))
    }
}

class CubicSplinePath(vararg poses: Pose) : SplinePath(*poses) {
    override fun createSpline(start: DifferentiablePoint2d, end: DifferentiablePoint2d): Spline {
        return Spline(Cubic(start.x, end.x), Cubic(start.y, end.y))
    }
}
