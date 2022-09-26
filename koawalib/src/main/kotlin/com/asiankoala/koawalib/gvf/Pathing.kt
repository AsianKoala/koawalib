package com.asiankoala.koawalib.gvf

import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.math.epsilonEquals
import org.apache.commons.math3.linear.LUDecomposition
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix
import kotlin.math.*

/**
 * in case it wasn't obvious, this is my fork of
 * the rr path generation system
 */

val coeffMatrix: RealMatrix = MatrixUtils.createRealMatrix(
    arrayOf(
        doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 1.0),
        doubleArrayOf(0.0, 0.0, 0.0, 0.0, 1.0, 0.0),
        doubleArrayOf(0.0, 0.0, 0.0, 2.0, 0.0, 0.0),
        doubleArrayOf(1.0, 1.0, 1.0, 1.0, 1.0, 1.0),
        doubleArrayOf(5.0, 4.0, 3.0, 2.0, 1.0, 0.0),
        doubleArrayOf(20.0, 12.0, 6.0, 2.0, 0.0, 0.0)
    )
)

object Pathing {
    class QuinticPolynomial(
        start: Double,
        startDeriv: Double,
        startSecondDeriv: Double,
        end: Double,
        endDeriv: Double,
        endSecondDeriv: Double,
    ) {
        private val a: Double
        private val b: Double
        private val c: Double
        private val d: Double
        private val e: Double
        private val f: Double

        operator fun get(t: Double) = (a * t + b) * (t * t * t * t) + c * (t * t * t) + d * (t * t) + e * t + f
        fun deriv(t: Double) = (5 * a * t + 4 * b) * (t * t * t) + (3 * c * t + 2 * d) * t + e
        fun secondDeriv(t: Double) = (20 * a * t + 12 * b) * (t * t) + 6 * c * t + 2 * d
        fun thirdDeriv(t: Double) = (60 * a * t + 24 * b) * t + 6 * c

        override fun toString() = String.format("%.5f*t^5+%.5f*t^4+%.5f*t^3+%.5f*t^2+%.5f*t+%.5f", a, b, c, d, e, f)

        init {
            val target =
                MatrixUtils.createRealMatrix(
                    arrayOf(
                        doubleArrayOf(
                            start,
                            startDeriv,
                            startSecondDeriv,
                            end,
                            endDeriv,
                            endSecondDeriv
                        )
                    )
                ).transpose()

            val solver = LUDecomposition(coeffMatrix).solver
            val coeff = solver.solve(target)

            a = coeff.getEntry(0, 0)
            b = coeff.getEntry(1, 0)
            c = coeff.getEntry(2, 0)
            d = coeff.getEntry(3, 0)
            e = coeff.getEntry(4, 0)
            f = coeff.getEntry(5, 0)
        }
    }

    abstract class ParametricCurve {

        operator fun get(s: Double, t: Double = reparam(s)) = internalGet(t)

        fun deriv(s: Double, t: Double = reparam(s)) = internalDeriv(t) * paramDeriv(t)

        private fun secondDeriv(s: Double, t: Double = reparam(s)): Vector {
            val deriv = internalDeriv(t)
            val secondDeriv = internalSecondDeriv(t)

            val paramDeriv = paramDeriv(t)
            val paramSecondDeriv = paramSecondDeriv(t)

            return secondDeriv * paramDeriv * paramDeriv +
                deriv * paramSecondDeriv
        }

        private fun thirdDeriv(s: Double, t: Double = reparam(s)): Vector {
            val deriv = internalDeriv(t)
            val secondDeriv = internalSecondDeriv(t)
            val thirdDeriv = internalThirdDeriv(t)

            val paramDeriv = paramDeriv(t)
            val paramSecondDeriv = paramSecondDeriv(t)
            val paramThirdDeriv = paramThirdDeriv(t)

            return thirdDeriv * paramDeriv * paramDeriv * paramDeriv +
                secondDeriv * paramSecondDeriv * paramDeriv * 3.0 +
                deriv * paramThirdDeriv
        }

        fun tangentAngle(s: Double, t: Double = reparam(s)) = deriv(s, t).angle
        fun tangentAngleDeriv(s: Double, t: Double = reparam(s)): Double {
            val deriv = deriv(s, t)
            val secondDeriv = secondDeriv(s, t)
            return deriv.x * secondDeriv.y - deriv.y * secondDeriv.x
        }
        fun tangentAngleSecondDeriv(s: Double, t: Double = reparam(s)): Double {
            val deriv = deriv(s, t)
            val thirdDeriv = thirdDeriv(s, t)
            return deriv.x * thirdDeriv.y - deriv.y * thirdDeriv.x
        }
        abstract fun length(): Double
        internal abstract fun reparam(s: Double): Double
        internal abstract fun internalGet(t: Double): Vector
        internal abstract fun internalDeriv(t: Double): Vector
        internal abstract fun internalSecondDeriv(t: Double): Vector
        internal abstract fun internalThirdDeriv(t: Double): Vector
        internal abstract fun paramDeriv(t: Double): Double
        internal abstract fun paramSecondDeriv(t: Double): Double
        internal abstract fun paramThirdDeriv(t: Double): Double
    }

    abstract class HeadingInterpolator {
        protected lateinit var curve: ParametricCurve

        open fun init(curve: ParametricCurve) {
            this.curve = curve
        }

        operator fun get(s: Double, t: Double = curve.reparam(s)) = internalGet(s, t)
        fun deriv(s: Double, t: Double = curve.reparam(s)) = internalDeriv(s, t)
        internal abstract fun internalGet(s: Double, t: Double): Double
        internal abstract fun internalDeriv(s: Double, t: Double): Double
        internal abstract fun internalSecondDeriv(s: Double, t: Double): Double
    }

    class TangentInterpolator constructor(
        internal val offset: Double = 0.0
    ) : HeadingInterpolator() {
        override fun internalGet(s: Double, t: Double) = (offset + curve.tangentAngle(s, t)).angleWrap

        override fun internalDeriv(s: Double, t: Double) = curve.tangentAngleDeriv(s, t)

        override fun internalSecondDeriv(s: Double, t: Double) = curve.tangentAngleSecondDeriv(s, t)
    }

    class Knot constructor(
        val x: Double,
        val y: Double,
        val dx: Double = 0.0,
        val dy: Double = 0.0,
        val d2x: Double = 0.0,
        val d2y: Double = 0.0
    ) {
        constructor(
            pos: Vector,
            deriv: Vector = Vector(),
            secondDeriv: Vector = Vector()
        ) : this(pos.x, pos.y, deriv.x, deriv.y, secondDeriv.x, secondDeriv.y)

        fun pos() = Vector(x, y)
    }

    class QuinticSpline(
        start: Knot,
        end: Knot,
        private val maxDeltaK: Double = 0.01,
        private val maxSegmentLength: Double = 0.25,
        private val maxDepth: Int = 30
    ) : ParametricCurve() {
        private val x: QuinticPolynomial = QuinticPolynomial(start.x, start.dx, start.d2x, end.x, end.dx, end.d2x)
        private val y: QuinticPolynomial = QuinticPolynomial(start.y, start.dy, start.d2y, end.y, end.dy, end.d2y)
        private var length: Double = 0.0
        private val sSamples = mutableListOf(0.0)
        private val tSamples = mutableListOf(0.0)

        init {
            parameterize(0.0, 1.0)
        }

        private fun approxLength(v1: Vector, v2: Vector, v3: Vector): Double {
            val w1 = (v2 - v1) * 2.0
            val w2 = (v2 - v3) * 2.0
            val det = w1.x * w2.y - w2.x * w1.y
            val chord = v1.dist(v3)
            return if (det epsilonEquals 0.0) {
                chord
            } else {
                val x1 = v1.x * v1.x + v1.y * v1.y
                val x2 = v2.x * v2.x + v2.y * v2.y
                val x3 = v3.x * v3.x + v3.y * v3.y

                val y1 = x2 - x1
                val y2 = x2 - x3

                val origin = Vector(y1 * w2.y - y2 * w1.y, y2 * w1.x - y1 * w2.x) / det
                val radius = origin.dist(v1)
                2.0 * radius * asin(chord / (2.0 * radius))
            }
        }

        private fun internalCurvature(t: Double): Double {
            val deriv = internalDeriv(t)
            val derivNorm = deriv.norm
            val secondDeriv = internalSecondDeriv(t)
            return (secondDeriv.x * deriv.y - deriv.x * secondDeriv.y).absoluteValue / (derivNorm * derivNorm * derivNorm)
        }

        private fun parameterize(
            tLo: Double,
            tHi: Double,
            vLo: Vector = internalGet(tLo),
            vHi: Vector = internalGet(tHi),
            depth: Int = 0
        ) {
            val tMid = 0.5 * (tLo + tHi)
            val vMid = internalGet(tMid)

            val deltaK = (internalCurvature(tLo) - internalCurvature(tHi)).absoluteValue
            val segmentLength = approxLength(vLo, vMid, vHi)

            if (depth < maxDepth && (deltaK > maxDeltaK || segmentLength > maxSegmentLength)) {
                parameterize(tLo, tMid, vLo, vMid, depth + 1)
                parameterize(tMid, tHi, vMid, vHi, depth + 1)
            } else {
                length += segmentLength
                sSamples.add(length)
                tSamples.add(tHi)
            }
        }

        override fun internalGet(t: Double) = Vector(x[t], y[t])

        override fun internalDeriv(t: Double) = Vector(x.deriv(t), y.deriv(t))

        override fun internalSecondDeriv(t: Double) =
            Vector(x.secondDeriv(t), y.secondDeriv(t))

        override fun internalThirdDeriv(t: Double) =
            Vector(x.thirdDeriv(t), y.thirdDeriv(t))

        private fun interp(s: Double, sLo: Double, sHi: Double, tLo: Double, tHi: Double) =
            tLo + (s - sLo) * (tHi - tLo) / (sHi - sLo)

        override fun reparam(s: Double): Double {
            if (s <= 0.0) return 0.0
            if (s >= length) return 1.0

            var lo = 0
            var hi = sSamples.size

            while (lo <= hi) {
                val mid = (hi + lo) / 2

                when {
                    s < sSamples[mid] -> {
                        hi = mid - 1
                    }
                    s > sSamples[mid] -> {
                        lo = mid + 1
                    }
                    else -> {
                        return tSamples[mid]
                    }
                }
            }

            return interp(s, sSamples[lo], sSamples[hi], tSamples[lo], tSamples[hi])
        }

        override fun paramDeriv(t: Double): Double {
            val deriv = internalDeriv(t)
            return 1.0 / sqrt(deriv.x * deriv.x + deriv.y * deriv.y)
        }

        override fun paramSecondDeriv(t: Double): Double {
            val deriv = internalDeriv(t)
            val secondDeriv = internalSecondDeriv(t)
            val numerator = -(deriv.x * secondDeriv.x + deriv.y * secondDeriv.y)
            val denominator = deriv.x * deriv.x + deriv.y * deriv.y
            return numerator / (denominator * denominator)
        }

        override fun paramThirdDeriv(t: Double): Double {
            val deriv = internalDeriv(t)
            val secondDeriv = internalSecondDeriv(t)
            val thirdDeriv = internalThirdDeriv(t)

            val firstNumeratorSqrt = 2.0 * (deriv.x * secondDeriv.x + deriv.y * secondDeriv.y)
            val secondNumerator = secondDeriv.x * secondDeriv.x + secondDeriv.y * secondDeriv.y +
                deriv.x * thirdDeriv.x + deriv.y * thirdDeriv.y

            val denominator = deriv.x * deriv.x + deriv.y * deriv.y
            return firstNumeratorSqrt * firstNumeratorSqrt / denominator.pow(3.5) -
                secondNumerator / denominator.pow(2.5)
        }

        override fun length() = length
        override fun toString() = "($x,$y)"
    }

    class PathSegment constructor(
        private val curve: ParametricCurve,
        val interpolator: HeadingInterpolator = TangentInterpolator()
    ) {
        init {
            interpolator.init(curve)
        }
        fun length() = curve.length()
        operator fun get(s: Double, t: Double = reparam(s)) = Pose(curve[s, t], interpolator[s, t])
        fun deriv(s: Double, t: Double = reparam(s)) = Pose(curve.deriv(s, t), interpolator.deriv(s, t))
        private fun tangentAngle(s: Double, t: Double = reparam(s)) = curve.tangentAngle(s, t)
        internal fun internalDeriv(s: Double, t: Double = reparam(s)) =
            Pose(curve.internalDeriv(t), interpolator.internalDeriv(s, t))
        internal fun internalSecondDeriv(s: Double, t: Double = reparam(s)) =
            Pose(curve.internalSecondDeriv(t), interpolator.internalDeriv(s, t))

        fun reparam(s: Double) = curve.reparam(s)
        fun end() = get(length())
        fun endTangentAngle() = tangentAngle(length())
    }

    class Path(private val segments: List<PathSegment>) {
        fun length() = segments.sumOf { it.length() }

        fun segment(s: Double): Pair<PathSegment, Double> {
            if (s <= 0.0) {
                return segments.first() to 0.0
            }
            var remainingDisplacement = s
            for (segment in segments) {
                if (remainingDisplacement <= segment.length()) {
                    return segment to remainingDisplacement
                }
                remainingDisplacement -= segment.length()
            }
            return segments.last() to segments.last().length()
        }

        operator fun get(s: Double, t: Double = reparam(s)): Pose {
            val (segment, remainingDisplacement) = segment(s)
            return segment[remainingDisplacement, t]
        }

        fun deriv(s: Double, t: Double = reparam(s)): Pose {
            val (segment, remainingDisplacement) = segment(s)
            return segment.deriv(remainingDisplacement, t)
        }

        internal fun internalDeriv(s: Double, t: Double = reparam(s)): Pose {
            val (segment, remainingDisplacement) = segment(s)
            return segment.internalDeriv(remainingDisplacement, t)
        }

        internal fun internalSecondDeriv(s: Double, t: Double = reparam(s)): Pose {
            val (segment, remainingDisplacement) = segment(s)
            return segment.internalSecondDeriv(remainingDisplacement, t)
        }

        private fun reparam(s: Double): Double {
            val (segment, remainingDisplacement) = segment(s)
            return segment.reparam(remainingDisplacement)
        }

        fun fastProject(queryPoint: Vector, projectGuess: Double = length() / 2.0): Double {
            var s = projectGuess
            repeat(200) {
                val t = reparam(s)
                val pathPoint = get(s, t).vec
                val deriv = deriv(s, t).vec

                val ds = (queryPoint - pathPoint) dot deriv

                if (ds epsilonEquals 0.0) {
                    return@repeat
                }

                s += ds

                if (s <= 0.0) {
                    return@repeat
                }

                if (s >= length()) {
                    return@repeat
                }
            }

            return max(0.0, min(s, length()))
        }

        fun start() = get(0.0)
        fun end() = get(length())
    }

    class PathBuilder private constructor(
        startPose: Pose?,
        startTangent: Double?,
        private val path: Path?,
        private val s: Double?
    ) {
        constructor(startPose: Pose, startTangent: Double = startPose.heading) :
            this(startPose, startTangent, null, null)

        constructor(startPose: Pose, reversed: Boolean) :
            this(startPose, (startPose.heading + if (reversed) PI else 0.0).angleWrap)

        private var currentPose: Pose? = startPose
        private var currentTangent: Double? = startTangent

        private var segments = mutableListOf<PathSegment>()

        private fun makeSpline(endPosition: Vector, endTangent: Double): QuinticSpline {
            val startPose = if (currentPose == null) {
                path!![s!!]
            } else {
                currentPose!!
            }

            val derivMag = (startPose.vec.dist(endPosition))
            val (startWaypoint, endWaypoint) = if (currentPose == null) {
                val startDeriv = path!!.internalDeriv(s!!).vec
                val startSecondDeriv = path.internalSecondDeriv(s).vec
                Knot(startPose.vec, startDeriv, startSecondDeriv) to
                    Knot(endPosition, Vector.fromPolar(derivMag, endTangent))
            } else {
                Knot(startPose.vec, Vector.fromPolar(derivMag, currentTangent!!)) to
                    Knot(endPosition, Vector.fromPolar(derivMag, endTangent))
            }

            return QuinticSpline(startWaypoint, endWaypoint)
        }

        private fun makeTangentInterpolator(curve: ParametricCurve): TangentInterpolator {
            if (currentPose == null) {
                val prevInterpolator = path!!.segment(s!!).first.interpolator as TangentInterpolator
                return TangentInterpolator(prevInterpolator.offset)
            }

            val startHeading = curve.tangentAngle(0.0, 0.0)
            val interpolator = TangentInterpolator(currentPose!!.heading - startHeading)
            interpolator.init(curve)
            return interpolator
        }

        private fun addSegment(segment: PathSegment): PathBuilder {
            currentPose = segment.end()
            currentTangent = segment.endTangentAngle()
            segments.add(segment)
            return this
        }

        fun splineTo(endPosition: Vector, endTangent: Double): PathBuilder {
            val spline = makeSpline(endPosition, endTangent)
            val interpolator = makeTangentInterpolator(spline)

            return addSegment(PathSegment(spline, interpolator))
        }

        fun build(): Path {
            return Path(segments)
        }
    }
}
