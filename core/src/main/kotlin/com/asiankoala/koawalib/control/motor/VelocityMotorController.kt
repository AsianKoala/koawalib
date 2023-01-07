package com.asiankoala.koawalib.control.motor

import com.asiankoala.koawalib.control.controller.PIDGains
import com.asiankoala.koawalib.control.profile.MotionState
import kotlin.math.absoluteValue

internal class VelocityMotorController(
    pid: PIDGains,
    private var kF: Double,
    private val allowedVelocityError: Double,
) : MotorController(pid, FFGains()) {
    override fun setTarget(requestedState: MotionState) {
        controller.reset()
        controller.targetPosition = requestedState.v
        targetState = requestedState
    }

    override fun isAtTarget(): Boolean = (currentState.v - targetState.v).absoluteValue < allowedVelocityError

    override fun update() {
        output = controller.update(currentState.v) + kF * currentState.v
    }
}
