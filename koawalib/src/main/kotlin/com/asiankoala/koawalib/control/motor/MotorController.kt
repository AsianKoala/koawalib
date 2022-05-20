package com.asiankoala.koawalib.control.motor

import com.acmerobotics.roadrunner.control.PIDFController
import com.asiankoala.koawalib.control.controller.PIDGains
import com.asiankoala.koawalib.control.profile.MotionState
import com.asiankoala.koawalib.subsystem.odometry.KEncoder

internal abstract class MotorController(
    pidGains: PIDGains,
    ffGains: FFGains,
    private val encoder: KEncoder,
) {
    protected abstract fun setTarget(requestedState: MotionState)
    abstract fun isAtTarget(): Boolean
    abstract fun update()

    protected val controller = PIDFController(
        pidGains.coeffs,
        ffGains.kV,
        ffGains.kA,
        ffGains.kS
    )

    var output = 0.0
    protected var currentState = MotionState(encoder.pos)
    protected var targetState = MotionState(encoder.pos)

    internal fun updateEncoder() {
        encoder.update()
        currentState = MotionState(encoder.pos, encoder.vel)
    }

    fun setProfileTarget(x: Double, v: Double = 0.0) {
        setTarget(MotionState(x, v))
    }

    fun setTargetPosition(x: Double) {
        setTarget(MotionState(x))
    }

    fun setTargetVelocity(v: Double) {
        setTarget(MotionState(v = v))
    }
}