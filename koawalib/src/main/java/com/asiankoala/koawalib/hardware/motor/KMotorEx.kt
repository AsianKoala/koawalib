package com.asiankoala.koawalib.hardware.motor

import com.asiankoala.koawalib.control.Controller
import com.asiankoala.koawalib.control.MotionProfileController
import com.asiankoala.koawalib.control.OpenLoopController
import com.asiankoala.koawalib.control.PIDExController

/**
 * Extended KMotor implementation
 * @see KMotor
 */
class KMotorEx(
    name: String,
    private val controller: Controller
) : KMotor(name) {

    override fun setSpeed(speed: Double) {
        if (controller is OpenLoopController) {
            controller.setDirectOutput(power)
        } else {
            throw IllegalArgumentException("MOTOR IS NOT OPEN LOOP")
        }
    }

    fun setPIDTarget(targetPosition: Double, targetVelocity: Double = 0.0, targetAcceleration: Double = 0.0) {
        if (controller is PIDExController) {
            controller.setControllerTargets(targetPosition, targetVelocity, targetAcceleration)
        } else {
            throw IllegalArgumentException("MOTOR IS NOT PID CONTROLLED")
        }
    }

    fun setMotionProfileTarget(targetPosition: Double) {
        if (controller is MotionProfileController) {
            controller.generateAndFollowMotionProfile(controller.currentPosition, 0.0, targetPosition, 0.0)
        } else {
            throw IllegalArgumentException("MOTOR IS NOT MOTION PROFILED")
        }
    }

    private var disabled: Boolean = false

    fun enable() {
        disabled = false
    }

    fun disable() {
        disabled = true
    }

    fun isAtTarget(): Boolean {
        return if (controller is PIDExController) {
            controller.isAtTarget
        } else {
            true
        }
    }

    fun update() {
        if (disabled) {
            super.setSpeed(0.0)
        } else {
            if (controller is PIDExController) {
                controller.measure(position, velocity)
                controller.update()
            }

            super.setSpeed(controller.output)
        }
    }
}
