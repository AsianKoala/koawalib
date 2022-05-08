package com.asiankoala.koawalib

import com.asiankoala.koawalib.math.EPSILON
import kotlin.test.assertEquals

fun assert(x: Double, y: Double) = assertEquals(x, y, absoluteTolerance = EPSILON)
fun assert(p: Pair<Double, Double>) = assert(p.first, p.second)
fun assert(xs: List<Double>, ys: List<Double>) {
    assertEquals(xs.size, ys.size)
    xs.zip(ys).forEach { assert(it) }
}
