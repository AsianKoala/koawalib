package com.asiankoala.koawalib.rework

import com.asiankoala.koawalib.control.motion.MotionConstraints
import com.asiankoala.koawalib.control.motion.MotionProfile
import com.asiankoala.koawalib.control.motion.MotionState
import com.asiankoala.koawalib.hardware.motor.KMotor
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.VOLTAGE_CONSTANT
import com.asiankoala.koawalib.math.assertPositive
import com.asiankoala.koawalib.subsystem.odometry.KEncoder
import com.qualcomm.robotcore.util.ElapsedTime
import kotlin.math.absoluteValue
import kotlin.math.sign

@Suppress("unused")
abstract class ComplexMotor(
    settings: ComplexMotorSettings,
    var kS: Double,
    var kV: Double,
    var kA: Double,
    var constraints: MotionConstraints,
    var allowedPositionError: Double,
    var allowedVelocityError: Double = Double.POSITIVE_INFINITY,
    var disabledPosition: Double? = null,
) : KMotor(settings.name) {
    var output = 0.0; private set
    var voltage = 0.0; private set

    val encoder = KEncoder(this, settings.ticksPerUnit, settings.isRevEncoder)
    val controller = PIDController(settings._kP, settings._kI, settings._kD)
    var setpointMotionState: MotionState = MotionState(); private set
    var pidOutput = 0.0; private set
    var isPIDEnabled = false

    var batteryScaledOutput = 0.0; private set
    private var isUsingVoltageFF = false
    private val voltageSensor = hardwareMap.voltageSensor.iterator().next()
    abstract val calculateFeedforward: Double
    var ffOutput = 0.0; private set
    var isFFEnabled = false

    var motionTimer = ElapsedTime(); private set
    var currentMotionProfile: MotionProfile? = null; private set
    var currentMotionState: MotionState? = null; private set
    var finalTargetMotionState: MotionState? = null; private set
    var isFollowingProfile = false; private set

    private fun isInDisabledZone(): Boolean {
        // if we don't have a disabled position or still in motion
        if (disabledPosition == null) return false
        // if our setpoint isn't in the deadzone
        if ((setpointMotionState.x - disabledPosition!!).absoluteValue > allowedPositionError) return false

        return isAtTarget(disabledPosition!!)
    }

    fun isAtTarget(target: Double = finalTargetMotionState!!.x): Boolean {
        return (encoder.pos - target).absoluteValue < allowedPositionError
    }

    fun isVelocityAtTarget(target: Double = finalTargetMotionState!!.v): Boolean {
        return (encoder.vel - target).absoluteValue < allowedVelocityError
    }

    fun isCompletelyFinished(): Boolean {
        return !isFollowingProfile && isAtTarget() && isVelocityAtTarget()
    }

    fun build(): ComplexMotor = this

    fun setTarget(x: Double, v: Double = 0.0) {
        controller.reset()
        motionTimer.reset()
        val startState = MotionState(encoder.pos, encoder.vel)
        val endState = MotionState(x, v)
        val profile = MotionProfile(startState, endState, constraints)

        currentMotionProfile = profile
        currentMotionState = startState
        finalTargetMotionState = endState

        isFollowingProfile = true
    }

    fun update() {
        encoder.update()

        if (isFollowingProfile) {
            val secIntoProfile = motionTimer.seconds()

            when {
                currentMotionProfile == null -> Logger.logError("MUST BE FOLLOWING MOTION PROFILE")

                secIntoProfile > currentMotionProfile!!.profileDuration -> {
                    isFollowingProfile = false
                    currentMotionProfile = null
                    setpointMotionState = finalTargetMotionState!!
                }

                else -> {
                    setpointMotionState = currentMotionProfile!![secIntoProfile]
                    controller.target = setpointMotionState.x
                }
            }
        }

        pidOutput = controller.update(encoder.pos)

        val rawFFOutput = kS * setpointMotionState.v.sign +
            kV * setpointMotionState.v +
            kA * setpointMotionState.a +
            calculateFeedforward

        ffOutput = rawFFOutput / VOLTAGE_CONSTANT

        val realPIDOutput = if (isPIDEnabled) pidOutput else 0.0
        val realFFOutput = if (isFFEnabled) ffOutput else 0.0

        output = realPIDOutput + realFFOutput

        super.power = when {
            isInDisabledZone() -> 0.0
            isUsingVoltageFF -> {
                voltage = voltageSensor.voltage
                batteryScaledOutput = output * (12.0 / voltage)
                batteryScaledOutput
            }

            else -> output
        }
    }

    init {
        assertPositive(kS)
        assertPositive(kV)
        assertPositive(kA)
        assertPositive(constraints.vMax)
        assertPositive(constraints.aMax)
        assertPositive(constraints.dMax)
        assertPositive(allowedPositionError)
        assertPositive((allowedVelocityError))
    }
}

