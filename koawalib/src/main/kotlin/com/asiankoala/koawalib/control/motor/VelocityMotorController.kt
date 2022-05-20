package com.asiankoala.koawalib.control.motor

import com.asiankoala.koawalib.control.controller.PIDGains
import com.asiankoala.koawalib.control.profile.MotionState
import com.asiankoala.koawalib.subsystem.odometry.KEncoder
import kotlin.math.absoluteValue

internal class VelocityMotorController(
    encoder: KEncoder,
    pid: PIDGains,
    var kF: Double,
    private val allowedVelocityError: Double,
) : MotorController(pid, FFGains(), encoder) {
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