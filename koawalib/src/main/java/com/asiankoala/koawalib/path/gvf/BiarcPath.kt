package com.asiankoala.koawalib.path.gvf

import com.asiankoala.koawalib.math.*
import kotlin.math.*

fun biarcInterpolate(p1: Pose, p2: Pose): Pair<Biarc.BiarcPart, Biarc.BiarcPart> {
    val v = p1 - p2
    val t1 = p1.directionVector()
    val t2 = p2.directionVector()
    val t = t1 + t2
    val discriminant = v.dot(t).pow(2) + 2 * (1 - t1.dot(t2)) * v.dot(v)
    val denom = 2 * (1 - t1.dot(t2))
    val d2: Double
    if (t1 == t2) {
        d2 = v.dot(v) / (4 * v.dot(t2))
    } else if (denom == 0.0) {
        val pm = p1 + v.scalarMul(0.5)
        val c1 = p1 + v.scalarMul(0.25)
        val c2 = p1 + v.scalarMul(0.75)
        val r = v.norm() / 4
        val theta1 = if (v.zProd(t2) < 0) PI else -PI
        val theta2 = if (v.zProd(t2) > 0) PI else -PI
        val start1 = (p1 - pm).atan2
        val end1 = start1 + theta1
        val start2 = (p2 - c2).atan2
        val end2 = start2 + theta2

        return Pair(Biarc.ArcSegment(c1, r, start1, end1), Biarc.ArcSegment(c2, r, start2, end2))
    } else {
        d2 = (-(v.dot(t)) + discriminant.pow(0.5)) / denom
    }
    val pm = (p1 + p2 + (t1 - t2).scalarMul(d2)).scalarMul(0.5)

    fun calcHalfBiarc(t: Point, p: Point, direction: Double): Biarc.BiarcPart {
        val n = t.getLeftNormal()
        val pmp1 = pm - p
        if (pmp1.dot(n) == 0.0) {
            return if (direction > 0) Biarc.LineSegment(p, pm) else Biarc.LineSegment(pm, p)
        } else {
            val s = (pmp1).dot(pmp1) / (n.scalarMul(2.0).dot(pmp1))
            val c = p + n.scalarMul(s)

            if (s == 0.0) {
                return Biarc.ArcSegment(c, s.absoluteValue, 0.0, 0.0)
            }
            val r = s.absoluteValue
            val op = (p - c).scalarMul(1 / r)
            val om = (pm - c).scalarMul(1 / r)
            val zProd = op.zProd(om)
            val theta: Double
            if (d2 > 0 && zProd > 0) {
                theta = acos(op.dot(om))
            } else if (d2 > 0 && zProd <= 0) {
                theta = -acos(op.dot(om))
            } else if (d2 <= 0 && zProd > 0) {
                theta = -2 * PI + acos(op.dot(om))
            } else {
                theta = 2 * PI - acos(op.dot(om))
            }
            val beginAngle: Double
            if (direction > 0) {
                beginAngle = (p - c).atan2
            } else {
                beginAngle = (pm - c).atan2
            }
            var endAngle = beginAngle + theta * direction
            endAngle = toHeading(endAngle)
            return Biarc.ArcSegment(c, r, beginAngle, endAngle)
        }
    }

    return Pair(calcHalfBiarc(t1, p1, 1.0), calcHalfBiarc(t2, p2, -1.0))
}

class BiarcPath(waypoints: Array<Pose>) : GVFPath() {
    override fun length(): Double {
        return totalLen
    }

    val segments = arrayOfNulls<Biarc.BiarcPartWrapper>((waypoints.size - 1) * 2)
    val totalLen: Double

    init {
        val segmentsUnWrapped = arrayOfNulls<Biarc.BiarcPart>((waypoints.size - 1) * 2)
        for (i in 0..waypoints.size - 2) {
            val wp1 = waypoints.get(i)
            val wp2 = waypoints.get(i + 1)
            val (part1, part2) = biarcInterpolate(wp1, wp2)
            segmentsUnWrapped.set(2 * i, part1)
            segmentsUnWrapped.set(2 * i + 1, part2)
        }
        totalLen = segmentsUnWrapped.sumByDouble { it!!.length() }
        var lastEnd = 0.0
        segmentsUnWrapped.forEachIndexed { i, biarcPart ->
            val begin = lastEnd
            val normalizedLen = biarcPart!!.length() / totalLen
            val end = begin + normalizedLen
            segments.set(i, Biarc.BiarcPartWrapper(biarcPart, begin, end))
            lastEnd = end
        }
    }

    private fun getSegment(t: Double): Biarc.BiarcPartWrapper {
        for (segment in segments) {
            if (segment == null) {
                continue
            }
            if (t in segment.begin..segment.end) {
                return segment
            }
        }
        throw IllegalArgumentException("t outside domain")
    }

    override fun closestTOnPathTo(r: Point, guess: Double): Double {
        var minDist = Double.MAX_VALUE
        var minT: Double? = null
        segments.forEach {
            try {
                val testT = it!!.project(r)
                val testPt = it.r(testT)
                val testDist = testPt.sqDist(r)
                if (testDist < minDist) {
                    minDist = testDist
                    minT = testT
                }
            } catch (e: IllegalArgumentException) {
                // Do nothing, point outside projection domain
            }
        }
        if (minT == null) {
            throw IllegalArgumentException("Point outside projection domain")
        }
        return minT as Double
    }

    override fun calculatePoint(t: Double): Point {
        return getSegment(t).r(t)
    }

    override fun tangentVec(t: Double): Point {
        return getSegment(t).tangentVec(t)
    }

    override fun levelSet(r: Point, closestT: Double): Double {
        // Phi
        val pathPt = calculatePoint(closestT)
        val pathTangentVec = tangentVec(closestT)
        val ptPathVec = (r - pathPt).normalized().neg()
        val sgn = sign(pathTangentVec.zProd(ptPathVec))
        return sgn * r.dist(pathPt)
    }

    override fun errorGradient(r: Point, closestT: Double): Point {
        return nVec(r, closestT)
    }

    override fun nVec(r: Point, closestT: Double): Point {
        return tangentVec(closestT).getRightNormal()
    }
}
