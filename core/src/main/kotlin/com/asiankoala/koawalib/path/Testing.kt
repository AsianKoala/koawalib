package com.asiankoala.koawalib.path

import com.asiankoala.koawalib.control.controller.ADRC
import com.asiankoala.koawalib.control.profile.MotionState
import com.asiankoala.koawalib.math.d
import com.qualcomm.robotcore.util.ElapsedTime
import jetbrains.letsPlot.elementRect
import jetbrains.letsPlot.export.ggsave
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.ggplot
import jetbrains.letsPlot.theme

object Testing {
    @JvmStatic
    fun main(args: Array<String>) {
//        val t = QuinticHermitePath(
//            Pose(-60.0, -10.0, 0.0),
//            Pose(-10.0, -35.0, 270.0.radians),
//            Pose(-30.0, -60.0, 180.0.radians),
//            Pose(-59.0, -10.0, 90.0.radians)
//        )
//
//        var lastProj = 0.0
//        for(i in 0..t.length.toInt()) {
//            val s = i.toDouble()
// //            val real = t[s].vec
// //            val projV = t.project(t[s].vec, lastS)
// //            lastS = projV
// //            val proj = t[projV].vec
//
//            val real = t[s].vec
//
//            val deriv = t[s, 1].vec
//            val normal = deriv.rotate(PI / 2.0)
//            val fakeRobot = normal.unit * 1.0 + real
//            val proj = t.project(fakeRobot, lastProj)
//            lastProj = proj
//            val projPoint = t[proj].vec
//            val rVec = projPoint - fakeRobot
//            val error = rVec.norm * (rVec cross deriv).sign
//            val kN = 0.7
//            val gvf = (deriv - normal * error * kN).unit
//            println("x: $fakeRobot, p: $projPoint, r: $rVec, e: ${String.format("%.2f", error)}, gvf: $gvf")
// //            println("real: $rVec, proj: $proj, projV: $projV, deriv: $deriv, normal: $normal, delta: ${rVec.dist(proj)}")
//        }
//        println(t.length)

        // val oldArc = OldArc(Vector(0.0), Vector(25.0, 25.0), Vector(50.0, 0.0))
        // val newArc = Arc(Vector(0.0), Vector(25.0, 25.0), Vector(50.0, 0.0))
        // println(oldArc.get(0.5))
        // println(newArc.get(0.5))

        // f    = 2x^2 + x + 5   @ 2 = 15
        // f'   = 4x + 1         @ 2 = 9
        // f''  = 4              @ 2 = 4
//        val vec = SimpleMatrix(6, 1, true,
//        doubleArrayOf(5.0, 2.0, -3.0, 10.0, 4.0, -6.0))
//
//        println(vec)
//        println(vec.numElements)
//        val coeffs = MutableList(vec.numElements, init = { index -> vec[index] })
//        println(coeffs)
//        val polynomial = Polynomial(
//            SimpleMatrix(6, 1, true,
//        doubleArrayOf(5.0, 2.0, -3.0, 10.0, 4.0, -6.0)
//        ))
//
//        val y = polynomial[2.0, 0]
//        println(" = $y")
//        println(polynomial)

//        val path = QuinticPath(Pose(), Pose(24.0, 24.0))
//        val x = path.interpolator.piecewiseCurve[0].x
//        val y = path.interpolator.piecewiseCurve[0].y
//        println(x[0.0, 1])
//        println(x.coeffs[5])

//        val a = HeadingController { 0.0 }
//        val b = HeadingControllerThatWorks { 0.0 }
//        b.flipBruh()
//        a.flip

        val adrc = ADRC(dt, 1.0 / 0.028, 0.5, 10.0)
        var u = 0.0
        val sample = 1000 * 2
        val ts = ArrayList<Double>()
        val ys = ArrayList<Double>()
        val us = ArrayList<Double>()
        val rs = ArrayList<Double>()
        val r1 = 1.0
        val system = System()
        for(i in 0..sample) {
            val y = system.update(u)
            u = adrc.call(y, u, r1)
            ts.add(i.d / 1000.0)
            ys.add(y)
            us.add(u)
            rs.add(r1)
        }

        plot(ts, ys, us, rs)
        println(ts.last())
        println(rs.last())
        println(ys.last())
    }


    private fun plot(
        t: ArrayList<Double>,
        a: ArrayList<Double>,
        b: ArrayList<Double>,
        c: ArrayList<Double>
    ) {
        val data = mapOf<String, Any>("t" to t, "y" to a, "u" to b, "r" to c)
        val fig = ggplot(data) +
                geomPoint(color = "blue", size = 1.0) { x = "t"; y = "y" } +
                geomPoint(color = "green", size = 1.0) { x = "t"; y = "u" } +
                geomPoint(color = "red", size = 1.0) { x = "t"; y = "r" } +
                theme(plotBackground = elementRect(fill = "black"))
        ggsave(fig, "plot.png")
    }
}

class System {
    private val m = 0.028
    private val g = 9.8
    private var pos = 0.0
    private var vel = 0.0
    private var accel = 0.0

    val state get() = MotionState(pos, vel, accel)

    fun update(u: Double): Double {
        accel = u * (1.0 / m) - g
        vel += accel * dt
        pos += vel * dt
        return pos
    }
}

const val dt = 0.001
