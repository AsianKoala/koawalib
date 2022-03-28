package com.asiankoala.koawalib.control.motion

data class MotionConstraints(
    var vMax: Double,
    var aMax: Double,
    var dMax: Double,
    var minCruiseTime: Double
)
