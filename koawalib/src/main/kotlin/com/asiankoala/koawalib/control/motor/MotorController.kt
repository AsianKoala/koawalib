package com.asiankoala.koawalib.control.motor

import com.acmerobotics.roadrunner.control.PIDFController
import com.asiankoala.koawalib.control.controller.Bounds
import com.asiankoala.koawalib.control.controller.PIDGains
import com.asiankoala.koawalib.control.profile.MotionState
import com.asiankoala.koawalib.hardware.motor.KEncoder
import com.asiankoala.koawalib.logger.Logger

internal abstract class MotorController(
    pidGains: PIDGains,
    ffGains: FFGains,
    private val encoder: KEncoder,
    bounds: Bounds = Bounds()
) {
    protected abstract fun setTarget(requestedState: MotionState)
    abstract fun isAtTarget(): Boolean
    abstract fun update()

    protected val controller = PIDFController(
        pidGains.coeffs,
        ffGains.kV,
        ffGains.kA,
        ffGains.kS
    ).apply {
        if(bounds.isBounded) {
            this.setInputBounds(bounds.lowerBound!!, bounds.upperBound!!)
            Logger.logInfo("set bounds")
        }
    }

    var output = 0.0; protected set
    protected var currentState = MotionState(encoder.pos)
    protected var targetState = MotionState(encoder.pos)

    fun updateEncoder() {
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
