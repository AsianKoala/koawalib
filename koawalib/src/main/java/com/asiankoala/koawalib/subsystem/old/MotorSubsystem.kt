package com.asiankoala.koawalib.subsystem.old

import com.acmerobotics.roadrunner.control.PIDCoefficients
import com.acmerobotics.roadrunner.control.PIDFController
import com.acmerobotics.roadrunner.profile.MotionProfile
import com.acmerobotics.roadrunner.profile.MotionProfileGenerator
import com.acmerobotics.roadrunner.profile.MotionState
import com.asiankoala.koawalib.subsystem.DeviceSubsystem
import com.asiankoala.koawalib.util.Logger
import com.qualcomm.robotcore.util.ElapsedTime
import kotlin.math.absoluteValue

/**
 * @see com.asiankoala.koawalib.hardware.motor.KMotorEx
 * @see com.asiankoala.koawalib.control.PIDExController
 * @see com.asiankoala.koawalib.control.MotionProfileController
 */
@Deprecated("port of koawalib v0, do not use")
//@Suppress("unused")
open class MotorSubsystem(val config: MotorSubsystemConfig) : DeviceSubsystem() {
    private val motor = config.motor
    private val encoder = config.encoder

    private val controller = PIDFController(
        PIDCoefficients(config.kP, config.kI, config.kD),
        config.kV,
        config.kA,
        config.kStatic,
        config.kF
    )

    var disabled = true
    var output = 0.0
    private var motionTimer = ElapsedTime()
    private var currentMotionProfile: MotionProfile? = null
    private var currentMotionState: MotionState? = null

    private var isFollowingProfile = false

    val isAtTarget: Boolean
        get() {
            if(encoder == null) {
                Logger.logError("encoder in subsystem $name is null")
                throw Exception()
            }
            return (encoder.position - controller.targetPosition).absoluteValue < config.positionEpsilon
        }

    /**
     * 1. is there a valid home position?
     * 2. is target position set to the home position?
     * 3. is the subsystem at home position?
     */
    private fun isHomed(): Boolean {
        return (!config.homePositionToDisable.isNaN() &&
                (controller.targetPosition - config.homePositionToDisable).absoluteValue < config.positionEpsilon &&
                (config.homePositionToDisable - encoder!!.position).absoluteValue < config.positionEpsilon)
    }

    private fun PIDFController.targetMotionState(state: MotionState) {
        targetPosition = state.x
        targetVelocity = state.v
        targetAcceleration = state.a
    }

    fun setPIDTarget(target: Double) {
        controller.reset()
        controller.targetPosition = target
    }

    fun generateAndFollowMotionProfile(start: Double, end: Double) {
        val startState = MotionState(start, 0.0)
        val endState = MotionState(end, 0.0)

        currentMotionProfile = MotionProfileGenerator.generateSimpleMotionProfile(
            startState,
            endState,
            config.maxVelocity,
            config.maxAcceleration,
            0.0
        )

        isFollowingProfile = false
        controller.reset()
        motionTimer.reset()
    }

    fun generateAndFollowMotionProfile(target: Double) {
        generateAndFollowMotionProfile(encoder!!.position, target)
    }

    fun setSpeedDirectly(speed: Double) {
        motor.setSpeed(speed)
    }

    override fun periodic() {
        encoder?.update()

        if(config.controlType != MotorControlType.OPEN_LOOP) {
            if(encoder == null) {
                Logger.logError("encoder in subsystem $name is null")
                throw Exception()
            }

            if (config.controlType == MotorControlType.MOTION_PROFILE && isFollowingProfile) {
                when {
                    currentMotionProfile == null -> throw Exception("MUST BE FOLLOWING A MOTION PROFILE!!!")

                    motionTimer.seconds() > currentMotionProfile!!.duration() -> {
                        isFollowingProfile = false
                        currentMotionProfile = null
                        currentMotionState = null
                    }

                    else -> {
                        currentMotionState = currentMotionProfile!![motionTimer.seconds()]
                        controller.targetMotionState(currentMotionState!!)
                    }
                }
            }

            output = if (disabled || isHomed()) {
                0.0
            } else {
                controller.update(encoder.position)
            }
        }

        Logger.addTelemetryData("$name output power", output)
        motor.setSpeed(output)
    }

}