package com.asiankoala.koawalib

import com.asiankoala.koawalib.math.EPSILON
import kotlin.test.assertEquals

fun assert(x: Double, y: Double) = assertEquals(x, y, absoluteTolerance = EPSILON)
