package com.asiankoala.koawalib.path.gvf

import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.*
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.LUDecomposition
import kotlin.math.*

class SplinePath(waypoints: Array<Pose>): Path() {
    override fun length(): Double {
        return length
    }

    override fun closestTOnPathTo(r: Point, guess: Double): Double {
        return closestTOnPathToArcs(r, guess)
    }
    fun closestTOnPathToGradient(r: Point, guess: Double): Double {
        val t = minimize({ t: Double -> calculatePoint(t).sqDist(r) }, guess)
        return t
    }
    fun closestTOnPathToArcs(r: Point, guess: Double): Double {
        var minDist = Double.MAX_VALUE
        var minT: Double? = guess
        polynomials.forEach {
            try {
                val testT = it.project(r)
                val testPt = it.eval(testT)
                val testDist = testPt.sqDist(r)
                if (testDist < minDist) {
                    minDist = testDist
                    minT = testT
                }
            }
            catch (e: IllegalArgumentException) {
                // Do nothing, point outside projection domain
            }
        }
        if (minT == null) {
            throw IllegalArgumentException("Point outside projection domain")
        }
        return minT as Double
    }
    fun getPartFor(t: Double): Part {
        polynomials.forEach {
            if (t in it) {
                return it
            }
        }
        throw IllegalArgumentException("t outside domain")
    }

    override fun calculatePoint(t: Double): Point {
        return getPartFor(t).eval(t)
    }

    override fun tangentVec(t: Double): Point {
        return getPartFor(t).tangentVec(t)
    }

    override fun levelSet(r: Point, closestT: Double): Double {
        val pathPt = calculatePoint(closestT)
        val pathTangentVec = tangentVec(closestT)
        val ptPathVec = (r - pathPt).normalized().neg()
        val sgn = (pathTangentVec.zProd(ptPathVec)).sign
        return sgn * r.dist(pathPt)
    }

    override fun errorGradient(r: Point, closestT: Double): Point {
        return nVec(r, closestT)
    }

    override fun nVec(r: Point, closestT: Double): Point {
        return tangentVec(closestT).getRightNormal()
    }

    fun curvature(t: Double): Double {
        return getPartFor(t).curvature(t)
    }

    val length: Double
    val polynomials: Array<Part>

    init {

        val polynomials_ = ArrayList<Part>()
        fun divide(p1: Pose, p2: Pose): List<Part> {
            val zVec = Point(0.0, 0.0)
            val polyPart = Part(p1, p2)/*
            if (polyPart.maxCurvature > 0.4) {
                val biarc = biarcInterpolate(p1, p2)
                val tOffset = 0.25
                val mid1Pt = biarc.first.r(tOffset)
                val mid1Tan = biarc.first.tangentVec(tOffset)
                val mid1Pose = math.Pose(mid1Pt.x, mid1Pt.y, mid1Tan.angle())
                val mid2Pt = biarc.second.r(1 - tOffset)
                val mid2Tan = biarc.second.tangentVec(1 - tOffset)
                val mid2Pose = math.Pose(mid2Pt.x, mid2Pt.y, mid2Tan.angle())
                return divide(p1, mid1Pose) + divide(mid1Pose, mid2Pose) + divide(mid2Pose, p2)
            }*/
            return arrayListOf(polyPart)
        }
        for (i in 0 until waypoints.size - 1) {
            val wp1 = waypoints[i]
            val wp2 = waypoints[i+1]
            polynomials_ += Part(wp1, wp2)

        }
        length = polynomials_.sumByDouble { it.length }
        var lenAccum = 0.0
        polynomials_.forEach {
            it.beginS = lenAccum
            lenAccum += it.length / length
            it.endS = lenAccum
        }
        polynomials = polynomials_.toTypedArray()

    }
    class Part(val wp1: Pose, val wp2: Pose) {
        val arcs: Array<Biarc.BiarcPartWrapper>
        val length: Double
        var beginS: Double = 0.0
        var endS: Double
        var doneWithParam = false

        val maxCurvature: Double
        private val poly: Polynomial

        private val refFrame = Pose(wp1.x, wp1.y, (wp2 - wp1).atan2)

        init {
            val wp2rf = refFrame.translate(wp2)
            val wp1rf = refFrame.translate(wp1)
            val ex = wp2rf.x
            endS = ex
            val mat = Array2DRowRealMatrix(arrayOf(
                doubleArrayOf( ex.pow(5),  ex.pow(4), ex.pow(3), ex.pow(2), ex, 1.0),
                doubleArrayOf( 0.0,  0.0, 0.0, 0.0, 0.0, 1.0),
                doubleArrayOf( 5.0 * ex.pow(4),  4.0 * ex.pow(3), 3.0 * ex.pow(2), 2.0 * ex, 1.0, 0.0),
                doubleArrayOf( 0.0,  0.0, 0.0, 0.0, 1.0, 0.0),
                doubleArrayOf(20.0 * ex.pow(3), 12.0 * ex.pow(2), 6.0 * ex, 2.0, 0.0, 0.0),
                doubleArrayOf( 0.0,  0.0, 0.0, 2.0, 0.0, 0.0)
            ))

            val vecB = ArrayRealVector(doubleArrayOf(wp2rf.y, 0.0, tan(wp2rf.heading), tan(wp1rf.heading), 0.0, 0.0))
            val arr = LUDecomposition(mat).solver.solve(vecB).toArray()
            poly = Polynomial(arr)

            // Find the length of the spline part
            val maxDK = 0.5
            val maxLen = 0.25
            var absCurvatureMax = 0.0
            fun subdivide(tBegin: Double, tEnd: Double, d: Int=0): Array<Biarc.BiarcPart> {
                val tMid = (tEnd + tBegin) / 2.0
                val pBegin = eval(tBegin)
                val pMid = eval(tMid)
                val pEnd = eval(tEnd)

                val kEnd = curvature(tEnd)
                val kBegin = curvature(tBegin)

                if (Point.arePointsCollinear(pBegin, pMid, pEnd)) {
                    if (pBegin.dist(pMid) + pMid.dist(pEnd) <= maxLen) {
                        return arrayOf(Biarc.AugmentedLine(pBegin, pEnd, kBegin, kEnd))
                    }
                    return subdivide(tBegin, tMid, d+1) + subdivide(tMid, tEnd, d+1)
                }

                val arc = Biarc.AugmentedArc.fromThreePoints(pBegin, pMid, pEnd, kBegin, kEnd)
                val doSubdivide = arc.length() > maxLen ||
                        (kEnd - kBegin).absoluteValue > maxDK


                if (doSubdivide) {
                    return subdivide(tBegin, tMid, d+1) + subdivide(tMid, tEnd, d+1)
                }
                if (kEnd.absoluteValue > absCurvatureMax) {
                    absCurvatureMax = kEnd.absoluteValue
                }
                else if (kBegin.absoluteValue > absCurvatureMax) {
                    absCurvatureMax = kBegin.absoluteValue
                }
                return arrayOf(arc)
            }

            arcs = subdivide(beginS, endS).map { Biarc.BiarcPartWrapper(it, 0.0, 1.0) }.toTypedArray()
            length = arcs.sumByDouble { it.length() }
            var lenAccum = 0.0
            arcs.forEachIndexed { index: Int, arc: Biarc.BiarcPartWrapper ->
                arc.begin = lenAccum
                val len_ = arc.length() / length
                arc.end = arc.begin + len_
                lenAccum += len_
            }
            maxCurvature = absCurvatureMax
            doneWithParam = true
        }
        private fun getRealS(s: Double): Double {
            return invLerp(beginS, endS, s)
        }
        private fun getArcFor(s: Double): Biarc.BiarcPartWrapper {
            arcs.forEach {
                if (s in it) {
                    return it
                }
            }
            if (s >= endS) {
                return arcs.last()
            }
            else if (s <= beginS) {
                return arcs.first()
            }
            throw IllegalArgumentException("$s not in domain $beginS .. $endS")
        }
        fun curvature(s: Double): Double {
            val s_ = getRealS(s)
            if (doneWithParam) {
                return getArcFor(s_).curvature(s_)
            }
            val slop = poly.derivative(s).pow(2)
            return poly.secondDerivative(s) / (1 + slop).pow(1.5)
        }
        fun eval(s: Double): Point {
            val s_ = getRealS(s)
            if (doneWithParam) {
                return getArcFor(s_).r(s_)
            }
            return refFrame.invTranslate(Point(s, poly.calculate(s)))
        }
        fun project(r: Point): Double {
            var minDist = Double.MAX_VALUE
            var minT: Double? = null
            arcs.forEach {
                try {
                    val testT = it.project(r)
                    val testPt = it.r(testT)
                    val testDist = testPt.dist(r)
                    if (testDist < minDist) {
                        minDist = testDist
                        minT = testT
                    }
                }
                catch (e: IllegalArgumentException) {
                    // Do nothing, point outside projection domain
                }
            }
            if (minT == null) {
                throw IllegalArgumentException("Point outside projection domain")
            }
            return lerp(beginS, endS, minT as Double)

        }

        operator fun contains(t: Double): Boolean {
            return t in beginS..endS
        }

        fun tangentVec(t: Double): Point {
            val s_ = getRealS(t)
            if (doneWithParam) {
                return getArcFor(s_).tangentVec(s_)
            }
            val angle = atan2(poly.derivative(t), 1.0)
            val realAngle = angle + this.refFrame.heading
            return Point.fromAngle(realAngle)
        }
    }
}
