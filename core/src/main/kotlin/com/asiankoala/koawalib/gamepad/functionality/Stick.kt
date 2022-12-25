package com.asiankoala.koawalib.gamepad.functionality

import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.util.Periodic

/**
 * Stick functionality
 * @property xAxis x value of stick
 * @property yAxis y value of stick
 * @property xSupplier function returning xAxis
 * @property ySupplier function returning yAxis
 * @property vector vector of xAxis, yAxis
 * @property angle angle of stick on [-180,180] interval
 * @property norm magnitude of stick
 */
internal interface Stick : Periodic {
    val xAxis: Double

    val yAxis: Double

    val xSupplier: () -> Double
        get() = this::xAxis

    val ySupplier: () -> Double
        get() = this::yAxis

    val vector: Vector
        get() = Vector(xAxis, yAxis)

    val angle: Double
        get() = vector.angle

    val norm: Double
        get() = vector.norm
}
