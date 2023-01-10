package com.asiankoala.koawalib.control.profile

data class MotionPeriod(
    val startState: MotionState,
    val dt: Double
) {
    val endState: MotionState = startState[dt]
    val dx: Double = endState.x - startState.x
    val flipped get() = MotionPeriod(endState.copy(v = -endState.v), dt)

    operator fun get(t: Double) = startState[t]
}
