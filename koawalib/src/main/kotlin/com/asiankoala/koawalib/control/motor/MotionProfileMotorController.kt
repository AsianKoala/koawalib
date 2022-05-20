package com.asiankoala.koawalib.control.motor

import com.asiankoala.koawalib.control.controller.PIDGains
import com.asiankoala.koawalib.control.profile.MotionConstraints
import com.asiankoala.koawalib.control.profile.MotionProfile
import com.asiankoala.koawalib.control.profile.MotionState
import com.asiankoala.koawalib.hardware.motor.KEncoder
import com.qualcomm.robotcore.util.ElapsedTime
import kotlin.math.absoluteValue

internal class MotionProfileMotorController(
    encoder: KEncoder,
    pid: PIDGains,
    private val ff: FFGains,
    private val constraints: MotionConstraints,
    private val allowedPositionError: Double,
    private val disabledPosition: DisabledPosition
) : MotorController(pid, ff, encoder) {
    private var profile: MotionProfile? = null
    private val timer = ElapsedTime()
    private var setpoint = MotionState(currentState.x)

    override fun setTarget(requestedState: MotionState) {
        controller.reset()
        timer.reset()
        targetState = requestedState
        profile = MotionProfile(currentState, targetState, constraints)
    }

    override fun isAtTarget(): Boolean = profile?.let {
        timer.seconds() >= it.duration && (currentState.x - targetState.x).absoluteValue < allowedPositionError
    } ?: false

    override fun update() {
        profile?.let { setpoint = it[timer.seconds()] }
        controller.apply {
            targetPosition = setpoint.x
            targetVelocity = setpoint.v
            targetAcceleration = setpoint.a
        }

        output = controller.update(currentState.x, currentState.v) + ff.calc(currentState.x)

        if (disabledPosition.shouldDisable(targetState.x, currentState.x)) {
            output = 0.0
        }
    }
}
