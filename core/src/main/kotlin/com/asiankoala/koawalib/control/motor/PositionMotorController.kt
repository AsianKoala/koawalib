package com.asiankoala.koawalib.control.motor

import com.asiankoala.koawalib.control.controller.Bounds
import com.asiankoala.koawalib.control.controller.PIDGains
import com.asiankoala.koawalib.control.profile.MotionState
import com.asiankoala.koawalib.logger.Logger
import kotlin.math.absoluteValue

internal class PositionMotorController(
    pid: PIDGains,
    private val ff: FFGains,
    private val allowedPositionError: Double,
    private val disabledPosition: DisabledPosition,
    bounds: Bounds
) : MotorController(pid, ff, bounds) {
    override fun setTarget(requestedState: MotionState) {
        controller.reset()
        controller.targetPosition = requestedState.x
        targetState = requestedState
    }

    override fun isAtTarget(): Boolean = (currentState.x - targetState.x).absoluteValue < allowedPositionError

    override fun update() {
        output = controller.update(currentState.x, currentState.v) + ff.calc(currentState.x)
        if (disabledPosition.shouldDisable(targetState.x, currentState.x, allowedPositionError)) {
            output = 0.0
            Logger.addTelemetryLine("controller disabled")
        }
    }
}
