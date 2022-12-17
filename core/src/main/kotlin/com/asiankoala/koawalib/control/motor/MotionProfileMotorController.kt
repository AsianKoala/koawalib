package com.asiankoala.koawalib.control.motor

import com.asiankoala.koawalib.control.controller.PIDGains
import com.asiankoala.koawalib.control.profile.MotionConstraints
import com.asiankoala.koawalib.control.profile.MotionProfile
import com.asiankoala.koawalib.control.profile.MotionState
import com.asiankoala.koawalib.logger.Logger
import com.qualcomm.robotcore.util.ElapsedTime
import kotlin.math.absoluteValue

internal class MotionProfileMotorController(
    pid: PIDGains,
    private val ff: FFGains,
    private val constraints: MotionConstraints,
    private val allowedPositionError: Double,
    private val disabledPosition: DisabledPosition,
) : MotorController(pid, ff) {
    private var profile: MotionProfile? = null
    private val timer = ElapsedTime()
    internal var setpoint = MotionState(currentState.x)
        private set

    override fun setTarget(requestedState: MotionState) {
        controller.reset()
        timer.reset()
        targetState = requestedState
        profile = MotionProfile.generateTrapezoidal(currentState.copy(v = 0.0, a = 0.0), targetState.copy(v = 0.0, a = 0.0), constraints)
        Logger.logInfo("created profile with startState", currentState)
        Logger.logInfo("created profile with endState", targetState)
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

        if (disabledPosition.shouldDisable(targetState.x, currentState.x, allowedPositionError)) {
            output = 0.0
            Logger.addTelemetryLine("controller disabled")
        }
    }
}
