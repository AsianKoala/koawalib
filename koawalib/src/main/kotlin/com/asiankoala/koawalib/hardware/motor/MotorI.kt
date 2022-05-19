package com.asiankoala.koawalib.hardware.motor

import com.asiankoala.koawalib.control.profile.MotionState

interface MotorI {
    var targetState: MotionState
    fun currentState(): MotionState
    fun isAtTarget(targetUnits: Double): Boolean
    fun isDisabled(): Boolean
    fun update()
}