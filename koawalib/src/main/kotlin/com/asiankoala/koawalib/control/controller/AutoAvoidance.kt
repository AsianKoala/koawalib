package com.asiankoala.koawalib.control.controller

import com.asiankoala.koawalib.math.Vector
import kotlin.math.PI

// see my desmos graph explaining how this works
// https://www.desmos.com/calculator/px3tbum5kb 
class AutoAvoidance(
    private val obstacles: List<Vector>,
    private val R: Double = 1.0,
    private val kN: Double = 1.0,
    private val ps: () -> Vector,
) {
    fun calculate(i: Vector): Vector {
        val p = ps.invoke()
        val o = obstacles.minByOrNull { (p - it).norm }!!
        val r = p - o
        val v1 = i.unit
        val theta = PI / 2.0
        val d1 = r dot v1
        val rm = r.norm
        val a = kN / rm
        val n1 = r.rotate(theta)
        val d2 = v1 dot n1
        val n2 = r.rotate(if(d2 >= 0) theta else -theta)
        val v2 = if(d1 >= 0.0 || r.norm > R) {
            v1
        } else {
            n2 + r * a
        }
        val im = i.norm
        val u = v2.unit
        return u / im
    }
}
