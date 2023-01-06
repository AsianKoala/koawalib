package com.asiankoala.koawalib.gamepad.functionality

import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.util.Periodic

/**
 * Stick functionality
 * @property xAxis x value of stick
 * @property yAxis y value of stick
 * @property vector vector of xAxis, yAxis
 */
internal interface Stick : Periodic {
    val xAxis: Double
    val yAxis: Double
    val vector get() = Vector(xAxis, yAxis)
}
