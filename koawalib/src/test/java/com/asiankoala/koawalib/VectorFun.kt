package com.asiankoala.koawalib

import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.wpilib.Matrix
import com.asiankoala.koawalib.wpilib.Numbers.N2
import com.asiankoala.koawalib.wpilib.Numbers.N1
import com.asiankoala.koawalib.wpilib.Numbers.N0
import com.qualcomm.robotcore.util.ElapsedTime
import org.ejml.simple.SimpleMatrix

object VectorFun {
    @JvmStatic
    fun main(args: Array<String>) {
        var m = 10.0
        var b = 5.0
        fun line(x: Double) = m * x + b
        val p = Vector(0.0, line(0.0))
        val r = Vector(10.0, line(10.0))
        m = -5.0
        b = 10.0
        val q = Vector(0.0, line(0.0))
        val s = Vector(-10.0, line(-10.0))

        val x1 = p.x
        val x2 = r.x
        val x3 = q.x
        val x4 = s.x
        val y1 = p.y
        val y2 = r.y
        val y3 = q.y
        val y4 = s.y

        val timer = ElapsedTime()
        timer.reset()
        var xTop = makeFourByFour(
            doubleArrayOf(x1, y1, x2, y2),
            doubleArrayOf(x1, 1.0, x2, 1.0),
            doubleArrayOf(x3, y3, x4, y4),
            doubleArrayOf(x3, 1.0, x4, 1.0)
        )

        var yTop = makeFourByFour(
            doubleArrayOf(x1, y1, x2, y2),
            doubleArrayOf(y1, 1.0, y2, 1.0),
            doubleArrayOf(x3, y3, x4, y4),
            doubleArrayOf(y3, 1.0, y4, 1.0)
        )

        val bottom = makeFourByFour(
            doubleArrayOf(x1, 1.0, x2, 1.0),
            doubleArrayOf(y1, 1.0, y2, 1.0),
            doubleArrayOf(x3, 1.0, x4, 1.0),
            doubleArrayOf(y3, 1.0, y4, 1.0)
        )

        println(timer.milliseconds())
        var intersect = Vector(xTop, yTop) / bottom
        println(intersect)

        timer.reset()
        val first = (x1 * y2 - y1 * x2)
        val second = (x3 * y4 - y3 * x4)
        xTop =  first * (x3 - x4) - (x1 - x2) * second
        yTop = first * (y3 - y4) - (y1 - y2) * second
        println(timer.milliseconds())
        intersect = Vector(xTop, yTop) / bottom
        println(intersect)
    }

    fun makeFourByFour(tl: DoubleArray, tr: DoubleArray, bl: DoubleArray, br: DoubleArray): Double {
        val topLeft = SimpleMatrix(2, 2, true, tl)
        val topRight = SimpleMatrix(2, 2,true, tr)
        val bottomLeft = SimpleMatrix(2,2,true, bl)
        val bottomRight = SimpleMatrix(2,2,true, br)
        return SimpleMatrix(2,2,true, doubleArrayOf(topLeft.determinant(), topRight.determinant(), bottomLeft.determinant(), bottomRight.determinant())).determinant()
    }


}