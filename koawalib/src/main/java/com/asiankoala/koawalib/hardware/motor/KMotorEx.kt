package com.asiankoala.koawalib.hardware.motor

import com.acmerobotics.roadrunner.control.PIDFController
import com.acmerobotics.roadrunner.profile.MotionProfile
import com.acmerobotics.roadrunner.profile.MotionProfileGenerator
import com.acmerobotics.roadrunner.profile.MotionState
import com.asiankoala.koawalib.command.CommandScheduler
import com.asiankoala.koawalib.command.commands.InfiniteCommand
import com.asiankoala.koawalib.control.MotorControlType
import com.asiankoala.koawalib.math.cos
import com.asiankoala.koawalib.subsystem.odometry.KEncoder
import com.asiankoala.koawalib.util.Logger
import com.qualcomm.robotcore.util.ElapsedTime
import kotlin.math.absoluteValue

@Suppress("unused")
class KMotorEx(private val config: KMotorExConfig) : KMotor(config.name) {
    private val encoder = KEncoder(this, config.ticksPerUnit, config.isRevEncoder)

    private val controller by lazy {
        val c = PIDFController(
            config.pid.asCoeffs,
            config.ff.kV,
            config.ff.kA,
            config.ff.kStatic,
            config.ff.kF
        )

        if(!config.lowerBound.isNaN() && !config.upperBound.isNaN()) {
            c.setInputBounds(config.lowerBound, config.upperBound)
        }

        c
    }

    private var output = 0.0
    private var motionTimer = ElapsedTime()
    private var currentMotionProfile: MotionProfile? = null
    private var currentMotionState: MotionState? = null
    private var isFollowingProfile = false

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
        return controller.update(encoder!!.position) +
                config.ff.kCos * controller.targetPosition.cos +
                config.ff.kTargetF(controller.targetPosition) +
                config.ff.kG
    }

    fun setPIDTarget(target: Double) {
        controller.reset()
        controller.targetPosition = target
    }

    fun followMotionProfile(motionProfile: MotionProfile) {
        currentMotionProfile = motionProfile
        isFollowingProfile = true
        controller.reset()
        motionTimer.reset()
    }

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

    fun followMotionProfile(startPosition: Double, endPosition: Double) {
        followMotionProfile(MotionState(startPosition, 0.0), MotionState(endPosition, 0.0))
    }

    fun followMotionProfile(targetPosition: Double) {
        followMotionProfile(encoder.position, targetPosition)
    }

    fun update() {
        encoder.update()

        if(config.controlType != MotorControlType.OPEN_LOOP) {

            if(config.controlType == MotorControlType.MOTION_PROFILE && isFollowingProfile) {
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

            output = if(isHomed()) {
                0.0
            } else {
                getControllerOutput()
            }
        }

        Logger.addTelemetryData("$deviceName output power", output)
        this.setSpeed(output)
    }

    init {
        CommandScheduler.scheduleForStart(InfiniteCommand(this::update))

    }
}