package com.asiankoala.koawalib.control.controller

import com.asiankoala.koawalib.control.motor.FFGains
import com.asiankoala.koawalib.control.profile.MotionConstraints
import com.asiankoala.koawalib.control.profile.MotionProfile
import com.asiankoala.koawalib.control.profile.MotionState
import com.asiankoala.koawalib.math.cos
import com.qualcomm.robotcore.util.ElapsedTime
import kotlin.math.sign

class ProfiledPIDController(
    var pid: PIDGains,
    var ff: FFGains,
    var constraints: MotionConstraints
) {
    private val controller = PIDFController(
        pid,
        ff.kV,
        ff.kA,
        ff.kS
    )
    private lateinit var profile: MotionProfile
    private var motionTimer = ElapsedTime()

    var targetState = MotionState()
    var currentState = MotionState()

    fun setTarget(current: Double, target: Double) {
        controller.reset()
        motionTimer.reset()
        currentState = MotionState(current)
        targetState = MotionState(target)
        profile = MotionProfile.generateFromConstraints(
            currentState,
            targetState,
            constraints
        )
    }

    fun update(position: Double, velocity: Double): Double {
        val setpoint = profile[motionTimer.seconds()]

        controller.apply {
            targetPosition = setpoint.x
            targetVelocity = setpoint.v
            targetAcceleration = setpoint.a
        }

        val pidOutput = controller.update(position, velocity)
        val ffOutput = ff.kS * setpoint.v.sign +
            ff.kV * setpoint.v +
            ff.kA * setpoint.a +
            (ff.kCos?.times(position.cos) ?: 0.0) +
            ff.kG

        return pidOutput + ffOutput
    }
}
