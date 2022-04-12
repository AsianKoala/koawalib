package com.asiankoala.koawalib.gamepad.functionality

import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.util.Periodic

interface Stick : Periodic {
    val xAxis: Double

    val yAxis: Double

    val xSupplier: () -> Double
        get() = this::xAxis

    val ySupplier: () -> Double
        get() = this::yAxis

    val vector: Vector
        get() = Vector(xAxis, yAxis)

    val angle: Double
        get() = vector.atan2

    val norm: Double
        get() = vector.hypot
}
