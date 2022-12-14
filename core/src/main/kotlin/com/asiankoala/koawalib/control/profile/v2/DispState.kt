package com.asiankoala.koawalib.control.profile.v2

import kotlin.math.sqrt

// monotonically increasing, starting at 0
data class DispState(
    val x: Double = 0.0,
    val v: Double = 0.0,
    val a: Double = 0.0
) {
    operator fun get(dx: Double) = DispState(
        x + dx,
        sqrt(v * v + 2.0 * a * dx),
        a
    )
}