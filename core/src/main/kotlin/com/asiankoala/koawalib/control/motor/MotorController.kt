package com.asiankoala.koawalib.control.motor

import com.asiankoala.koawalib.control.controller.Bounds
import com.asiankoala.koawalib.control.controller.PIDFController
import com.asiankoala.koawalib.control.controller.PIDGains
import com.asiankoala.koawalib.control.profile.MotionState
import kotlin.math.cos

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
        ffGains.kS,
        kF = { pos, _ -> ffGains.kG + (ffGains.kCos?.let { it * cos(pos) } ?: 0.0) }
    ).apply {
        if (bounds.isBounded) {
            this.setInputBounds(bounds.lowerBound!!, bounds.upperBound!!)
        }
    }

    var output = 0.0; protected set
    var targetState = MotionState(0.0); protected set
    var currentState = MotionState(0.0); internal set

    fun setProfileTarget(x: Double) {
        setTarget(MotionState(x))
    }

    fun setTargetPosition(x: Double) {
        setTarget(MotionState(x))
    }

    fun setTargetVelocity(v: Double) {
        setTarget(MotionState(v = v))
    }
}
