package com.asiankoala.koawalib.hardware.motor

import com.asiankoala.koawalib.control.PIDController
import com.asiankoala.koawalib.control.motion.MotionProfile
import com.asiankoala.koawalib.control.motion.MotionState
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.VOLTAGE_CONSTANT
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
    var voltage = 0.0; private set

    val encoder = KEncoder(this, settings.ticksPerUnit, settings.isRevEncoder)
    val controller = PIDController(settings._kP, settings._kI, settings._kD)
    var pidOutput = 0.0; private set

    var batteryScaledOutput = 0.0; private set
    private val voltageSensor = hardwareMap.voltageSensor.iterator().next()
    var ffOutput = 0.0; private set

    var motionTimer = ElapsedTime(); private set
    var currentMotionProfile: MotionProfile? = null; private set
    var setpointMotionState: MotionState = MotionState(); private set
    var currentMotionState: MotionState? = null; private set
    var finalTargetMotionState: MotionState? = null; private set
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

    fun isVelocityAtTarget(target: Double = finalTargetMotionState!!.v): Boolean {
        return (encoder.vel - target).absoluteValue < settings.allowedVelocityError
    }

    fun isCompletelyFinished(): Boolean {
        return !isFollowingProfile && isAtTarget() && isVelocityAtTarget()
    }

    val enableVoltageFF: KMotorEx
        get() {
            settings.isUsingVoltageFF = true
            return this
        }

    val disableVoltageFF: KMotorEx
        get() {
            settings.isUsingVoltageFF = false
            return this
        }

    fun setTarget(x: Double, v: Double = 0.0) {
        controller.reset()
        motionTimer.reset()

        if(settings.constraints == null) {
            settings.isMotionProfiled = false
            isFollowingProfile = false
            finalTargetMotionState = MotionState(x, 0.0, 0.0)
        } else {
            val startState = MotionState(encoder.pos, encoder.vel)
            val endState = MotionState(x, v)
            val profile = MotionProfile(startState, endState, settings.constraints!!)

            currentMotionProfile = profile
            currentMotionState = startState
            finalTargetMotionState = endState
            isFollowingProfile = true
        }
    }

    fun update() {
        encoder.update()

        if (isFollowingProfile) {
            if(settings.isMotionProfiled) {
                val secIntoProfile = motionTimer.seconds()

                when {
                    currentMotionProfile == null -> Logger.logError("MUST BE FOLLOWING MOTION PROFILE")

                    secIntoProfile > currentMotionProfile!!.duration -> {
                        isFollowingProfile = false
                        currentMotionProfile = null
                        setpointMotionState = finalTargetMotionState!!
                    }

                    else -> {
                        setpointMotionState = currentMotionProfile!![secIntoProfile]
                        controller.target = setpointMotionState.x
                        currentMotionState = MotionState(encoder.pos, encoder.vel, setpointMotionState.a)
                    }
                }
            } else {
                controller.target = finalTargetMotionState!!.x
                currentMotionState = MotionState(encoder.pos, encoder.vel, encoder.accel)
            }
        }


        pidOutput = controller.update(encoder.pos)

        val rawFFOutput = settings.kS * setpointMotionState.v.sign +
                settings.kV * setpointMotionState.v +
                settings.kA * setpointMotionState.a +
                settings.kG +
                if(settings.kCos epsilonNotEqual 0.0) settings.kCos * encoder.pos.cos else 0.0

        ffOutput = rawFFOutput / VOLTAGE_CONSTANT

        val realPIDOutput = if (settings.isPIDEnabled) pidOutput else 0.0
        val realFFOutput = if (settings.isFFEnabled) ffOutput else 0.0

        output = realPIDOutput + realFFOutput

        super.power = when {
            settings.isCompletelyDisabled || isInDisabledZone() -> 0.0
            settings.isUsingVoltageFF -> {
                voltage = voltageSensor.voltage
                batteryScaledOutput = output * (12.0 / voltage)
                batteryScaledOutput
            }

            else -> output
        }
    }
}
