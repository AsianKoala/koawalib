package com.asiankoala.koawalib.hardware.motor

import com.acmerobotics.roadrunner.util.epsilonEquals
import com.asiankoala.koawalib.control.Controller
import com.asiankoala.koawalib.control.MotionProfileController
import com.asiankoala.koawalib.control.OpenLoopController
import com.asiankoala.koawalib.control.PIDExController
import com.asiankoala.koawalib.util.Logger

/**
 * Extended KMotor implementation
 * @see KMotor
 */
@Suppress("unused")
class KMotorEx(
    name: String,
    private val controller: Controller
) : KMotor(name) {

    override fun setSpeed(speed: Double) {
        if (controller is OpenLoopController) {
            controller.output = speed
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

    private fun checkIsUpdating() {
        if(device.power epsilonEquals 0.0) {
            Logger.logWarning("motor $name may not be updating")
        }
    }

    fun enable() {
        disabled = false
        if(controller is PIDExController) {
            controller.reset()
        }
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
        val output = if (disabled) {
            Logger.logDebug("motor $name disabled")
            0.0
        } else {
            if (controller is PIDExController) {
                controller.measure(position, velocity)
                controller.output = controller.update()
            }

            controller.output
        }

        super.device.power = output
    }
}
