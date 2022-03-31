package com.asiankoala.koawalib

import com.asiankoala.koawalib.math.Spline
import kotlin.math.pow

object SplineTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val spline = Spline(2, doubleArrayOf(0.0, 0.0, 1.0, 0.0, 0.0, 1.0), false)

        val steps = 100
        for (i in 0 until steps) {
            val `val` = i.toDouble() / steps
            println(
                `val`.toString() + "\t" + spline.evaluate(`val`) + "\t" + `val`.pow(2.0) / 2.0
            )
        }
    }
}