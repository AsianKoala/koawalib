package com.asiankoala.koawalib.pathing

import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.clamp
import kotlin.math.pow
import kotlin.math.atan2
import kotlin.math.absoluteValue

data class DifferentiablePoint(
    val zero: Double = 0.0,
    val first: Double = 0.0,
    val second: Double = 0.0,
    val third: Double = 0.0
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

class Quintic(
    start: DifferentiablePoint,
    end: DifferentiablePoint
) : DifferentiableFunction() {
    private val coeffVec = mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

    override operator fun get(t: Double): DifferentiablePoint {
        return DifferentiablePoint(
            coeffVec[0] * t.pow(5) + coeffVec[1] * t.pow(4) + coeffVec[2] * t.pow(3) +
                    coeffVec[3] * t.pow(2) + coeffVec[4] * t + coeffVec[5],
            5 * coeffVec[0] * t.pow(4) + 4 * coeffVec[1] * t.pow(3) + 3 * coeffVec[2] * t.pow(2) +
                    2 * coeffVec[3] * t + coeffVec[4],
            20 * coeffVec[0] * t.pow(3) + 12 * coeffVec[1] * t.pow(2) + 6 * coeffVec[2] * t + 2 * coeffVec[3]
        )
    }

    init {
        /**
         * https://www.wolframalpha.com/input?i=row+echelon+form+%5B%5B0%2C0%2C0%2C0%2C0%2Cf%2Cu%5D%2C%5B0%2C0%2C0%2C0%2Ce%2C0%2Cv%5D%2C%5B0%2C0%2C0%2C2d%2C0%2C0%2Cw%5D%2C%5Ba%2Cb%2Cc%2Cd%2Ce%2Cf%2Cx%5D%2C%5B5a%2C4b%2C3c%2C2d%2C1e%2C0%2Cy%5D%2C%5B20a%2C12b%2C6c%2C2d%2C0%2C0%2Cz%5D%5D
         * tldr to find coeffs, just shove it into row echelon form
         * and then just calc it
         */

        // coeffs: a,b,c,d,e,f
        // u, v, w = start diff
        // x, y, z = end diff

        // 2 = 2u/f
        // 2f = 2u
        // f = u
        coeffVec[5] = start.zero

        // 2 = 2v/e
        // 2e = 2v
        // e = v
        coeffVec[4] = start.first

        // 2 = w/d
        // 2d = w
        // d = w/2
        coeffVec[3] = start.second / 2.0

        // 2 = -(20u + 12v + 3w - 20x + 8y - z) / c
        // c = -(20u + 12v + 3w - 20x + 8y - z) / 2
        coeffVec[2] = -(20 * start.zero + 12 * start.first + 3 * start.second
                - 20 * end.first + 8 * end.second - end.second) / 2.0

        // 2 = (30u + 16v + 3w -30x + 14y - 2z) / b
        // b = (30u + 16v + 3w -30x + 14y - 2z) / 2
        coeffVec[1] = (30 * start.zero + 16 * start.first + 3 * start.second
                - 30 * end.zero + 14 * end.first - 2 * end.second) / 2.0

        // 2 = -(12u + 6v + w - 12x + 6y -z) / a
        // a = -(12u + 6v + w - 12x + 6y -z) / 2
        coeffVec[0] = -(12 * start.zero + 6 * start.first + start.second
                - 12 * end.zero + 6 * end.first - end.second) / 2.0
    }
}

/*
see https://www.youtube.com/watch?v=unWguclP-Ds&list=PLC8FC40C714F5E60F&index=2
and https://pomax.github.io/bezierinfo/#arclength
arc length is int 0->1 sqrt(f_x(t)^2 + f_y(t)^2) dt of course
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
            length += curve[t, 1].norm * coefficient.weight
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
// by just chain ruling stuff
// d/ds r(s) =
// = d/ds (r(t(s)))
// = r'(t(s)) * t'(s)
// r'(t(s)) is just r'(t)
// = r'(t) * t'(s)
// t(s) is a pain to compute analytically, but since its an inverse to s(t),
// t'(s) = 1 / s'(t)
// so d/ds r(s) = r'(t) * (1 / s'(t))
// s'(t) = d/dt int 0->t |r'(u)| du
// = d/dt (|R'(t)| - |R'(0)|)
// = |r'(t)|
// so d/dt r(s) = r'(t) * (1 / |r'(t)|)
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
    var curvature: Double
        private set
    val ref: Vector
    val length: Double
    val angleOffset: Double
    private var curvatureSet = false
    private var dkds = 0.0
    private var tStart = 0.0
    private var tEnd = 0.0
    var dt = 0.0
        private set

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


class Spline(
    private val x: DifferentiableFunction,
    private val y: DifferentiableFunction
) {
    private var _length = 0.0
    private val arcs = mutableListOf<Arc>()
    val length: Double get() = _length

    operator fun get(t: Double, n: Int = 0): Vector {
        val xt = x[t]
        val yt = y[t]
        return when(n) {
            1 -> Vector(xt.first, yt.first)
            2 -> Vector(xt.second, yt.second)
            3 -> Vector(xt.third, yt.third)
            else -> Vector(xt.zero, yt.zero)
        }
    }

    // from multi: k = (a x v) / |v|^3
    fun getK(t: Double) = this[t, 2].cross(this[t, 1]) / this[t, 1].norm.pow(3)

    // now that we have our spline parametrized into arcs,
    // we can find the corresponding t with s by iterating across
    // our arc array and finding what iteration it is at
    // ok this is a shitty name but like... is it really?
    fun invArc(s: Double): Double {
        if(s < 0) return 0.0
        if(s > length) return 1.0
        var arcLengthSum = 0.0
        var intdt = 0.0
        var its = 0
        while(arcLengthSum < s) {
            val workingarc = arcs[its]
            if(arcLengthSum + workingarc.length > s) return intdt + workingarc.interpolateSAlongT(s)
            arcLengthSum += workingarc.length
            intdt += workingarc.dt
            its += 1
        }
        throw Exception("i think ur pretty fucking bad a coding neil")
    }


    // s'(t) = d/dt int 0->t |r'(u)| du
    // = |r'(t)|
    // expand |r'(t)|
    // s'(t) = |r'(t)|
    // = norm(x'(t), y'(t))
    // = sqrt(x'(t)^2 + y'(t)^2)
    // take another deriv
    // s''(t) = (1 / sqrt(x'(t)^2 + y'(t)^2)) * (2 * x'(t) * x''(t) + 2 * y'(t) * y''(t))
    // ok lets try to simplify that xdddd
    // 2 * x'(t) * x''(t) + 2 * y'(t) * y''(t) can factor 2 out
    // = 2 * (x'(t) * x''(t) + y'(t) * y''(t)) last part of this is just tDeriv dot tDeriv2
    // = 2 * tDeriv dot tDeriv2
    // and that denom is just s'(t)
    // s''(t) = (2 * tDeriv dot tDeriv2) / sDeriv(t)
    // i dont want to take another fucking derivative
    fun sDeriv(t: Double, n: Int = 1): Double {
        return when(n) {
            1 -> this[t, 1].norm
            2 -> (2 * this[t, 1].dot(this[t, 2])) / sDeriv(t) // recursion??? :face_with_raised_eyebrow:
            else -> throw Exception("im lazy and didn't want to implement more derivatives")
        }
    }

    fun deriv(s: Double, n: Int = 1): Vector {
        val t = invArc(s)
        return when(n) {
            1 -> this[t, 1].unit
            2 -> this[t, 2] * sDeriv(t).pow(2) + this[t, 1] * sDeriv(t, 2)
            else -> throw Exception("im lazy and didn't implement more derivatives")
        }
    }

    fun angle(s: Double, n: Int = 0): Double {
        return when(n) {
            0 -> deriv(s).angle
            1 -> deriv(s).cross(deriv(s, 2))
            else -> throw Exception("im lazy and didn't implement more derivatives")
        }
    }

    init {
        val tParams = ArrayDeque<Pair<Double, Double>>()
        tParams.add(Pair(0.0, 1.0))
        var its = 0

        while(tParams.isNotEmpty()) {
            val curr = tParams.first()
            tParams.removeFirst()

            val midT = (curr.first + curr.second) / 2.0
            val startV = this[curr.first]
            val midV = this[midT]
            val endV  = this[curr.second]
            val klo = getK(curr.first)
            val khi = getK(curr.second)
            val dk = (khi - klo).absoluteValue
            val arc = Arc(startV, midV, endV)
            // we want to make our arcs as linear?ish as possible to have
            // a more accurate interpolation when param from s -> t
            // might want to try adjusting 0.01 for curve or 1.0 for arc length later
            val subdivide = dk > 0.01 || arc.length > 1.0
            if(subdivide) {
                tParams.add(Pair(midT, curr.second))
                tParams.add(Pair(curr.first, midT))
            } else {
                arc.setCurvature(klo, khi)
                arc.setT(curr)
                arcs.add(arc)
                _length += arc.length
                its++
            }

            if(its > 1000) {
                throw Exception("we fucked up")
            }
        }
    }
}

class Path(
    start: Pose,
    vararg poses: Pose
) {
    private var _length = 0.0
    private val splines = mutableListOf<Spline>()

    val start get() = this[0.0]
    val end get() = this[_length]
    val length get() = _length

    private fun find(s: Double): Pair<Double, Spline> {
        if(s < 0.0) return Pair(0.0, splines[0])
        if(s > _length) return Pair(_length, splines[0])
        var accum = 0.0
        for(spline in splines) {
            if(accum + spline.length > s) {
                return Pair(s - accum, spline)
            }
            accum += spline.length
        }
        throw Exception("couldn't find shit bozo")
    }

    operator fun get(s: Double, n: Int = 0): Pose {
        val ret = find(s)
        return when(n) {
            0 -> Pose(ret.second[ret.second.invArc(s)], ret.second.angle(s))
            1 -> Pose(ret.second.deriv(s), ret.second.angle(s, 1))
            else -> throw Exception("fuck you im not adding more derivatives :rage:")
        }
    }

    /*
    stole this from rr
    basically the way this works is
    take rVec from pose to proj (proj = get(s))
    this is ideally normal to the curve
    check if its normal with by dot product with tangent vec
    ofc result is 0, its normal (and therefore the correct projection)
    if not, then add dot product to s
    since dot product literally just finds the amount vec a is parallel to vec b
     */
    fun project(p: Vector, pGuess: Double): Double {
        return (1..10).fold(pGuess) { s, _ ->
            clamp(s + (p - get(s).vec).dot(get(s, 1).vec), 0.0, length)
        }
    }

    init {
        var curr = start
        for(target in poses) {
            val cv = curr.vec
            val tv = target.vec
            val r = cv.dist(tv)
            val s = DifferentiablePoint2d(cv, Vector.fromPolar(r, curr.heading))
            val e = DifferentiablePoint2d(tv, Vector.fromPolar(r, target.heading))
            val xQuintic = Quintic(s.x, e.x)
            val yQuintic = Quintic(s.y, e.y)
            val spline = Spline(xQuintic, yQuintic)
            splines.add(spline)
            _length += spline.length
            curr = target
        }
    }
}

