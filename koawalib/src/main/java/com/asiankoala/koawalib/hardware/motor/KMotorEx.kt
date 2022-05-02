package com.asiankoala.koawalib.hardware.motor

import com.acmerobotics.roadrunner.control.PIDFController
import com.acmerobotics.roadrunner.profile.MotionProfile
import com.acmerobotics.roadrunner.profile.MotionProfileGenerator
import com.acmerobotics.roadrunner.profile.MotionState
import com.asiankoala.koawalib.command.KScheduler
import com.asiankoala.koawalib.command.commands.LoopCmd
import com.asiankoala.koawalib.control.FeedforwardConstants
import com.asiankoala.koawalib.control.MotorControlType
import com.asiankoala.koawalib.control.PIDConstants
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.cos
import com.asiankoala.koawalib.subsystem.odometry.KEncoder
import com.qualcomm.robotcore.util.ElapsedTime
import kotlin.math.absoluteValue

/**
 * Extended motor implementation. Supports open-loop control, PID+feedforward, and motion profiling
 * @see KMotorExConfig
 */
@Suppress("unused")
class KMotorEx(private val config: KMotorExConfig) : KMotor(config.name) {
    val encoder = KEncoder(this, config.ticksPerUnit, config.isRevEncoder)

    private val controller by lazy {
        val c = PIDFController(
            config.pid.asCoeffs,
            config.ff.kV,
            config.ff.kA,
            config.ff.kStatic,
            config.ff.kF
        )

        if (!config.lowerBound.isNaN() && !config.upperBound.isNaN()) {
            c.setInputBounds(config.lowerBound, config.upperBound)
        }

        c
    }

    private var output = 0.0
    private var motionTimer = ElapsedTime()
    private var currentMotionProfile: MotionProfile? = null
    private var currentMotionState: MotionState? = null
    private var isFollowingProfile = false

    /**
     * Return if abs(error) < position epsilon
     */
    val isAtTarget: Boolean
        get() {
            return (encoder.position - controller.targetPosition).absoluteValue < config.positionEpsilon
        }

    private fun isHomed(): Boolean {
        val hasHomePosition = !config.homePositionToDisable.isNaN()
        val isTargetingHomePosition = (controller.targetPosition - config.homePositionToDisable).absoluteValue < config.positionEpsilon
        val isAtHomePosition = (config.homePositionToDisable - encoder.position).absoluteValue < config.positionEpsilon
        return hasHomePosition && isTargetingHomePosition && isAtHomePosition
    }

    private fun PIDFController.targetMotionState(state: MotionState) {
        targetPosition = state.x
        targetVelocity = state.v
        targetAcceleration = state.a
    }

    private fun getControllerOutput(): Double {
        return controller.update(encoder.position) +
            config.ff.kCos * controller.targetPosition.cos +
            config.ff.kTargetF(controller.targetPosition) +
            config.ff.kG
    }

    internal fun update() {
        encoder.update()

        if (config.controlType != MotorControlType.OPEN_LOOP) {

            if (config.controlType == MotorControlType.MOTION_PROFILE && isFollowingProfile) {
                when {
                    currentMotionProfile == null -> Logger.logError("MUST BE FOLLOWING A MOTION PROFILE !!!!")

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

            output = if (isHomed()) {
                0.0
            } else {
                getControllerOutput()
            }
        }

        Logger.addTelemetryData("$deviceName output power", output)
        this.power = output
    }

    /**
     * Set PID controller target
     * @param target target setpoint of the pid controller
     */
    fun setPIDTarget(target: Double) {
        controller.reset()
        controller.targetPosition = target
    }

    /**
     * Follow a motion profile
     * @param motionProfile motion profile to follow
     */
    fun followMotionProfile(motionProfile: MotionProfile) {
        currentMotionProfile = motionProfile
        isFollowingProfile = true
        controller.reset()
        motionTimer.reset()
    }

    /**
     * Follow a motion profile created from a start and end state
     * @param startState start state of profile
     * @param endState end state of the profile
     */
    fun followMotionProfile(startState: MotionState, endState: MotionState) {
        val motionProfile = MotionProfileGenerator.generateSimpleMotionProfile(
            startState,
            endState,
            config.maxVelocity,
            config.maxAcceleration,
            0.0
        )

        followMotionProfile(motionProfile)
    }

    /**
     * Follow a motion profile from a start position to target position, assuming 0 velocity in start and end state
     * @param startPosition start position of the profile
     * @param endPosition end position of the profile
     */
    fun followMotionProfile(startPosition: Double, endPosition: Double) {
        followMotionProfile(MotionState(startPosition, 0.0), MotionState(endPosition, 0.0))
    }

    /**
     * Follow a motion profile from a target position. Assumes current position is the start position and 0 velocity at start/end state
     * @param targetPosition end position of the motion profile
     */
    fun followMotionProfile(targetPosition: Double) {
        followMotionProfile(encoder.position, targetPosition)
    }

    init {
        KScheduler.schedule(LoopCmd(this::update))
    }

    companion object {
        fun createMotor(
            name: String,
            ticksPerUnit: Double,
            isRevEncoder: Boolean,
            controlType: MotorControlType,

            pid: PIDConstants,
            ff: FeedforwardConstants,

            positionEpsilon: Double,
            homePositionToDisable: Double = Double.NaN,
            lowerBound: Double = Double.NaN,
            upperBound: Double = Double.NaN,
            maxVelocity: Double = Double.NaN,
            maxAcceleration: Double = Double.NaN,

        ): KMotorEx {
            return KMotorEx(
                KMotorExConfig(
                    name,
                    ticksPerUnit,
                    isRevEncoder,
                    controlType,
                    pid,
                    ff,
                    positionEpsilon,
                    homePositionToDisable,
                    lowerBound, upperBound,
                    maxVelocity,
                    maxAcceleration
                )
            )
        }
    }
}
