package com.asiankoala.koawalib.control

import com.asiankoala.koawalib.control.motion.MotionConstraints
import com.asiankoala.koawalib.control.motion.MotionProfile
import com.asiankoala.koawalib.control.motion.MotionState
import com.asiankoala.koawalib.hardware.motor.FFSettings
import com.asiankoala.koawalib.hardware.motor.PIDSettings
import com.asiankoala.koawalib.math.cos
import com.qualcomm.robotcore.util.ElapsedTime
import kotlin.math.sign

class ProfiledPIDController(
    var pid: PIDSettings,
    var ff: FFSettings,
    var constraints: MotionConstraints
) {
    private val controller = PIDController(pid.kP, pid.kI, pid.kD)
    private lateinit var profile: MotionProfile
    private var motionTimer = ElapsedTime()

    var targetState = MotionState()
    var currentState = MotionState()

    fun setTarget(current: Double, target: Double) {
        controller.reset()
        motionTimer.reset()
        currentState = MotionState(current)
        targetState = MotionState(target)
        profile = MotionProfile(
            currentState,
            targetState,
            constraints
        )
    }

    fun update(position: Double, velocity: Double): Double {
        val setpoint = profile[motionTimer.seconds()]

        controller.apply {
            kP = pid.kP
            kI = pid.kI
            kD = pid.kD
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