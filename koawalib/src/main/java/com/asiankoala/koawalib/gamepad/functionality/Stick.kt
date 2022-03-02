package com.asiankoala.koawalib.gamepad.functionality

import com.asiankoala.koawalib.math.Point
import com.asiankoala.koawalib.util.Periodic

interface Stick : Periodic {
    val xAxis: Double

    val yAxis: Double

    val xSupplier: () -> Double
        get() = this::xAxis

    val ySupplier: () -> Double
        get() = this::yAxis

    val point: Point
        get() = Point(xAxis, yAxis)

    val angle: Double
        get() = point.atan2

    val distanceFromCenter: Double
        get() = point.hypot
}
