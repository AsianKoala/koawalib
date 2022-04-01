package com.asiankoala.koawalib.hardware.motor

import com.acmerobotics.roadrunner.util.epsilonEquals
import com.asiankoala.koawalib.control.Controller
import com.asiankoala.koawalib.control.MotionProfileController
import com.asiankoala.koawalib.control.OpenLoopController
import com.asiankoala.koawalib.control.PIDExController
import com.asiankoala.koawalib.math.d
import com.asiankoala.koawalib.subsystem.odometry.Encoder
import com.asiankoala.koawalib.util.Logger

/**
 * Extended KMotor implementation
 * @see KMotor
 */
@Suppress("unused")
class KMotorEx(
    name: String,
    private val controller: PIDExController,
    private val encoder: Encoder
) : KMotor(name) {

    fun setPIDTarget(targetPosition: Double, targetVelocity: Double = 0.0, targetAcceleration: Double = 0.0) {
        controller.setControllerTargets(targetPosition, targetVelocity, targetAcceleration)
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
        if (device.power epsilonEquals 0.0) {
            Logger.logWarning("motor $name may not be updating")
        }
    }

    fun enable() {
        disabled = false
        controller.reset()
    }

    fun disable() {
        disabled = true
    }

    val isAtTarget get() = controller.isAtTarget

    fun update() {
        val output = if (disabled) {
            Logger.logDebug("motor $name disabled")
            0.0
        } else {
            controller.currentPosition = encoder.position
            controller.currentVelocity = encoder.velocity
            controller.output = controller.update()
            controller.output
        }

        setSpeed(output)
    }
}
