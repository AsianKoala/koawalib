package com.asiankoala.koawalib.control.motor

import com.asiankoala.koawalib.control.controller.PIDGains
import com.asiankoala.koawalib.control.profile.MotionState
import com.asiankoala.koawalib.math.cos
import com.asiankoala.koawalib.subsystem.odometry.KEncoder
import kotlin.math.absoluteValue

internal class PositionMotorController(
    encoder: KEncoder,
    pid: PIDGains,
    private val ff: FFGains,
    private val allowedPositionError: Double,
    private val disabledPosition: DisabledPosition
) : MotorController(pid, ff, encoder) {
    override fun setTarget(requestedState: MotionState) {
        controller.reset()
        targetState = requestedState
    }

    override fun isAtTarget(): Boolean = (currentState.x - targetState.x).absoluteValue < allowedPositionError

    override fun update() {
        output = controller.update(currentState.x, currentState.v) + ff.calc(currentState.x)

        if(disabledPosition.shouldDisable(targetState.x, currentState.x)) {
            output = 0.0
        }
    }
}