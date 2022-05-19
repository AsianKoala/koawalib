package com.asiankoala.koawalib.control.profile

data class MotionPeriod(
    var startState: MotionState,
    var dt: Double
) {
    val endState get() = startState[dt]

    operator fun get(t: Double) = startState[t]
}
