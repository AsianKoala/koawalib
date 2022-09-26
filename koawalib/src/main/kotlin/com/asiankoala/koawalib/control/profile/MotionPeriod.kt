package com.asiankoala.koawalib.control.profile

data class MotionPeriod(
    var startState: MotionState,
    var dt: Double
) {
    val endState get() = startState[dt]

    val dx = endState.x - startState.x

    operator fun get(t: Double) = startState[t]
}
