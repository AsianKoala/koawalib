package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.math.EPSILON
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.nonZeroSign
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin

object OdometryTest {
    @JvmStatic
    fun main(args: Array<String>) {
        var global = Pose()
        val inc = Pose(1.0)
        for (i in 0 until 10) global = exp(global, inc)
        println(global)
    }

    private fun rot(v: Vector, s: Double, c: Double) =
        Vector(s * v.x - c * v.y, c * v.x + s * v.y)

    private fun exp(global: Pose, inc: Pose): Pose {
        val u = inc.heading + nonZeroSign(inc.heading.sign) * EPSILON
        val s = sin(u) / u
        val c = (1.0 - cos(u)) / u
        val trans = rot(inc.vec, s, c)
        val theta = global.heading + inc.heading
        val delta = trans.rotate(theta)
        return Pose(global.vec + delta, theta)
    }
}
