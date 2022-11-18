package com.asiankoala.koawalib.control.motor

import com.asiankoala.koawalib.control.controller.Bounds
import com.asiankoala.koawalib.control.controller.PIDFController
import com.asiankoala.koawalib.control.controller.PIDGains
import com.asiankoala.koawalib.control.profile.MotionState
import com.asiankoala.koawalib.logger.Logger

internal abstract class MotorController(
    pidGains: PIDGains,
    ffGains: FFGains,
    bounds: Bounds = Bounds()
) {
    protected abstract fun setTarget(requestedState: MotionState)
    abstract fun isAtTarget(): Boolean
    abstract fun update()

    protected val controller = PIDFController(
        pidGains,
        ffGains.kV,
        ffGains.kA,
        ffGains.kS
    ).apply {
        if (bounds.isBounded) {
            this.setInputBounds(bounds.lowerBound!!, bounds.upperBound!!)
            Logger.logInfo("set bounds")
        }
    }

    var output = 0.0; protected set
    var targetState = MotionState(0.0); protected set
    var currentState = MotionState(0.0); internal set

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
