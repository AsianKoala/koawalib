package com.asiankoala.koawalib.hardware.motor

import com.asiankoala.koawalib.control.PIDController
import com.asiankoala.koawalib.control.motion.MotionProfile
import com.asiankoala.koawalib.control.motion.MotionState
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.cos
import com.asiankoala.koawalib.math.epsilonNotEqual
import com.asiankoala.koawalib.subsystem.odometry.KEncoder
import com.qualcomm.robotcore.util.ElapsedTime
import kotlin.math.absoluteValue
import kotlin.math.sign

@Suppress("unused")
class KMotorEx(
    val settings: KMotorExSettings,
) : KMotor(settings.name) {
    var output = 0.0; private set

    val encoder = KEncoder(this, settings.ticksPerUnit, settings.isRevEncoder).zero(settings.startingPosition)
    val controller = PIDController(settings.pid.kP, settings.pid.kI, settings.pid.kD)
    var pidOutput = 0.0; private set

    var batteryScaledOutput = 0.0; private set
    var ffOutput = 0.0; private set

    val motionTimer = ElapsedTime()
    var currentMotionProfile: MotionProfile? = null; private set
    var currentMotionState: MotionState = MotionState(settings.startingPosition); private set
    var setpointMotionState: MotionState = MotionState(settings.startingPosition); private set
    var finalTargetMotionState: MotionState = MotionState(settings.startingPosition); private set
    var isFollowingProfile = false; private set

    private fun isInDisabledZone(): Boolean {
        // if we don't have a disabled position or still in motion
        if (settings.disabledPosition == null) return false
        // if our setpoint isn't in the deadzone
        if ((setpointMotionState.x - settings.disabledPosition!!).absoluteValue > settings.allowedPositionError) return false

        return isAtTarget(settings.disabledPosition!!)
    }

    fun isAtTarget(target: Double = finalTargetMotionState!!.x): Boolean {
        return (encoder.pos - target).absoluteValue < settings.allowedPositionError
    }

    fun isCompletelyFinished(): Boolean {
        return !isFollowingProfile && isAtTarget()
    }

    val enableVoltageFF: KMotorEx
        get() {
            settings.isVoltageCorrected = true
            return this
        }

    val disableVoltageFF: KMotorEx
        get() {
            settings.isVoltageCorrected = false
            return this
        }

    fun setTarget(x: Double, v: Double = 0.0) {
        controller.reset()
        motionTimer.reset()

        isFollowingProfile = if (settings.constraints == null && !settings.isMotionProfiled) {
            finalTargetMotionState = MotionState(x, 0.0, 0.0)
            false
        } else {
            currentMotionState = MotionState(encoder.pos, encoder.vel)
            finalTargetMotionState = MotionState(x, v)
            currentMotionProfile = MotionProfile(
                currentMotionState,
                finalTargetMotionState, settings.constraints!!
            )
            true
        }
    }

    fun update() {
        encoder.update()

        controller.apply {
            kP = settings.pid.kP
            kI = settings.pid.kI
            kD = settings.pid.kD
        }

        currentMotionState = MotionState(encoder.pos, encoder.vel, encoder.accel)

        if (isFollowingProfile) {
            if (!settings.isMotionProfiled) Logger.logError("subsystem not motion profiled")
            if (currentMotionProfile == null) Logger.logError("MUST BE FOLLOWING MOTION PROFILE")

            val secIntoProfile = motionTimer.seconds()

            controller.targetPosition = if (secIntoProfile > currentMotionProfile!!.duration) {
                isFollowingProfile = false
                currentMotionProfile = null
                setpointMotionState = finalTargetMotionState
                finalTargetMotionState.x
            } else {
                setpointMotionState = currentMotionProfile!![secIntoProfile]
                controller.targetVelocity = setpointMotionState.v
                controller.targetAcceleration = setpointMotionState.a
                setpointMotionState.x
            }
        } else {
            controller.targetPosition = finalTargetMotionState.x
        }

        pidOutput = controller.update(
            currentMotionState.x,
            if (settings.isMotionProfiled && isFollowingProfile) currentMotionState.v else null
        )

        ffOutput = settings.ff.kS * setpointMotionState.v.sign +
            settings.ff.kV * setpointMotionState.v +
            settings.ff.kA * setpointMotionState.a +
            settings.ff.kG +
            if (settings.ff.kCos epsilonNotEqual 0.0) settings.ff.kCos * encoder.pos.cos else 0.0

        val realPIDOutput = if (settings.disabledSettings.isPIDDisabled) 0.0 else pidOutput
        val realFFOutput = if (settings.disabledSettings.isFFDisabled) 0.0 else ffOutput

        output = realPIDOutput + realFFOutput

        super.power = when {
            settings.disabledSettings.isCompletelyDisabled || isInDisabledZone() -> 0.0
            settings.isVoltageCorrected -> {
                batteryScaledOutput = output * (12.0 / lastVoltageRead)
                batteryScaledOutput
            }

            else -> output
        }
    }
}
