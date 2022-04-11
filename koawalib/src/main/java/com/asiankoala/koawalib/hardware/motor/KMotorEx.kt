package com.asiankoala.koawalib.hardware.motor

import com.acmerobotics.roadrunner.control.PIDFController
import com.acmerobotics.roadrunner.profile.MotionProfile
import com.acmerobotics.roadrunner.profile.MotionProfileGenerator
import com.acmerobotics.roadrunner.profile.MotionState
import com.asiankoala.koawalib.command.CommandScheduler
import com.asiankoala.koawalib.command.commands.InfiniteCommand
import com.asiankoala.koawalib.math.cos
import com.asiankoala.koawalib.subsystem.odometry.KEncoder
import com.asiankoala.koawalib.control.FeedforwardConstants
import com.asiankoala.koawalib.control.MotorControlType
import com.asiankoala.koawalib.control.PIDConstants
import com.asiankoala.koawalib.util.Logger
import com.asiankoala.koawalib.util.OpModeState
import com.qualcomm.robotcore.util.ElapsedTime
import kotlin.math.absoluteValue

class KMotorEx(
    name: String,
    private val encoder: KEncoder?,
    private val controlType: MotorControlType,

    private val pid: PIDConstants = PIDConstants(),
    private val ff: FeedforwardConstants = FeedforwardConstants(),

    private val positionEpsilon: Double,
    private val homePositionToDisable: Double = Double.NaN,
    private val lowerBound: Double = Double.NaN,
    private val upperBound: Double = Double.NaN,
    private val maxVelocity: Double = 0.0,
    private val maxAcceleration: Double = 0.0,
) : KMotor(name) {

    private val controller by lazy {
        val c = PIDFController(
            pid.asCoeffs,
            ff.kV,
            ff.kA,
            ff.kStatic,
            ff.kF
        )

        if(!lowerBound.isNaN() && !upperBound.isNaN()) {
            c.setInputBounds(lowerBound, upperBound)
        }

        c
    }

    var disabled = true
    private var output = 0.0
    private var motionTimer = ElapsedTime()
    private var currentMotionProfile: MotionProfile? = null
    private var currentMotionState: MotionState? = null
    private var isFollowingProfile = false

    val isAtTarget: Boolean
        get() {
            if(encoder == null) {
                Logger.logError("encoder for motor $deviceName is null")
            }
            return (encoder!!.position - controller.targetPosition).absoluteValue < positionEpsilon
        }

    private fun isHomed(): Boolean {
        val hasHomePosition = !homePositionToDisable.isNaN()
        val isTargetingHomePosition = (controller.targetPosition - homePositionToDisable).absoluteValue < positionEpsilon
        val isAtHomePosition = (homePositionToDisable - encoder!!.position).absoluteValue < positionEpsilon
        return hasHomePosition && isTargetingHomePosition && isAtHomePosition
    }

    private fun PIDFController.targetMotionState(state: MotionState) {
        targetPosition = state.x
        targetVelocity = state.v
        targetAcceleration = state.a
    }

    private fun getControllerOutput(): Double {
        return controller.update(encoder!!.position) +
                ff.kCos * controller.targetPosition.cos +
                ff.kTargetF(controller.targetPosition) +
                ff.kG
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
            maxVelocity,
            maxAcceleration,
            0.0
        )

        followMotionProfile(motionProfile)
    }

    fun followMotionProfile(startPosition: Double, endPosition: Double) {
        followMotionProfile(MotionState(startPosition, 0.0), MotionState(endPosition, 0.0))
    }

    fun followMotionProfile(targetPosition: Double) {
        followMotionProfile(encoder!!.position, targetPosition)
    }

    fun update() {
        encoder?.update()

        if(controlType != MotorControlType.OPEN_LOOP) {
            if(encoder == null) {
                Logger.logError("encoder for motor $deviceName is null")
            }

            if(controlType == MotorControlType.MOTION_PROFILE && isFollowingProfile) {
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
        if(!disabled) this.setSpeed(output)
    }

    init {
        CommandScheduler.scheduleForStart(InfiniteCommand(this::update))
    }
}