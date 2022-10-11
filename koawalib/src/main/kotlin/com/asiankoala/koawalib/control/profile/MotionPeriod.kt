package com.asiankoala.koawalib.control.profile

data class MotionPeriod(
    val startState: MotionState,
    val dt: Double
) {
    val endState: MotionState
    val dx: Double
    val flipped get() = MotionPeriod(endState.copy(v = -endState.v), -dt)

    operator fun get(t: Double) = startState[t]

    init {
        endState = startState[dt]
        dx = endState.x - startState.x
    }
}
