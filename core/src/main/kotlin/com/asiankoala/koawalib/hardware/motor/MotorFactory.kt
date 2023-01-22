package com.asiankoala.koawalib.hardware.motor

import com.asiankoala.koawalib.control.controller.Bounds
import com.asiankoala.koawalib.control.controller.PIDGains
import com.asiankoala.koawalib.control.motor.*
import com.asiankoala.koawalib.control.profile.MotionConstraints
import com.asiankoala.koawalib.control.profile.MotionState
import com.asiankoala.koawalib.logger.Logger
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple

class MotorFactory(name: String) {
    private val instance = KMotor(name)

    /**
     * Set the motor mode to brake
     */
    val brake: MotorFactory
        get() {
            instance.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            return this
        }

    /**
     * Set the motor mode to float
     */
    val float: MotorFactory
        get() {
            instance.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
            return this
        }

    /**
     * Set this motor's direction to forward (NOTE: Does not change encoder direction)
     */
    val forward: MotorFactory
        get() {
            instance.direction = DcMotorSimple.Direction.FORWARD
            return this
        }

    /**
     * Set this motor's direction to reverse (NOTE: Does not change encoder direction)
     */
    val reverse: MotorFactory
        get() {
            instance.direction = DcMotorSimple.Direction.REVERSE
            return this
        }

    /**
     * Enable voltage correction
     */
    val voltageCorrected: MotorFactory
        get() {
            instance.isVoltageCorrected = true
            return this
        }

    /**
     * Lower the priority of the motor, updating it 3x slower than the control loop
     */
    val lowPriority: MotorFactory
        get() {
            instance.priority = KMotor.Priority.LOW
            return this
        }

    /**
     * Incrase the priority of the motor, updating it at normal speed
     */
    val highPriority: MotorFactory
        get() {
            instance.priority = KMotor.Priority.HIGH
            return this
        }

    /**
     * Pair this motor's encoder with another motor's encoder
     */
    fun pairEncoder(
        motor: KMotor,
        encoderFactory: EncoderFactory
    ): MotorFactory {
        instance.encoder = encoderFactory.build(motor)
        instance.encoderCreated = true
        Logger.logInfo("encoder for motor ${instance.deviceName} paired with encoder on motor ${motor.deviceName}'s port")
        return this
    }

    /**
     * Created an encoder association with this motor
     */
    fun createEncoder(
        encoderFactory: EncoderFactory
    ) = pairEncoder(instance, encoderFactory)

    /**
     * Add a static feedforward term to the motor. This is included in motor power calculatiosn
     * regardless of motor mode (e.g. a motor with [MotorControlModes.OPEN_LOOP] with still
     * calculate static feedforward). Used to deal with motor problems.
     */
    fun withStaticFeedforward(ks: Double): MotorFactory {
        instance.ks = ks
        return this
    }

    /**
     * Enable position PID control in the motor
     */
    @JvmOverloads
    fun withPositionControl(
        pidGains: PIDGains,
        ffGains: FFGains,
        allowedPositionError: Double,
        disabledPosition: Double? = null,
        bounds: Bounds = Bounds(),
    ): MotorFactory {
        instance.mode = MotorControlModes.POSITION
        instance.controller = PositionMotorController(
            pidGains,
            ffGains,
            bounds,
            allowedPositionError,
            DisabledPosition(disabledPosition),
        )
        return this
    }

    /**
     * Enable velocity PID control in the motor
     */
    fun withVelocityControl(
        pidGains: PIDGains,
        kF: Double,
        allowedVelocityError: Double,
    ): MotorFactory {
        instance.mode = MotorControlModes.VELOCITY
        instance.controller = VelocityMotorController(pidGains, kF, allowedVelocityError)
        return this
    }

    /**
     * Enable motion profile control in the motor
     */
    @JvmOverloads
    fun withMotionProfileControl(
        pidGains: PIDGains,
        ffGains: FFGains,
        constraints: MotionConstraints,
        allowedPositionError: Double,
        disabledPosition: Double? = null,
        bounds: Bounds = Bounds(),
    ): MotorFactory {
        instance.mode = MotorControlModes.MOTION_PROFILE
        instance.controller = MotionProfileMotorController(
            pidGains,
            ffGains,
            bounds,
            allowedPositionError,
            DisabledPosition(disabledPosition),
            constraints,
        )
        return this
    }

    /**
     * Build the motor from the motor factory
     */
    fun build(): KMotor {
        if (!instance.encoderCreated && instance.mode != MotorControlModes.OPEN_LOOP)
            throw Exception()

        instance.enable()
        val information = "\n" +
            "name: ${instance.deviceName}\n" +
            "mode: ${instance.mode}\n" +
            "direction: ${instance.direction}\n" +
            "zeroPowerBehavior: ${instance.zeroPowerBehavior}\n"

        Logger.logInfo("scheduled motor with information: $information")

        instance.encoder?.let { enc ->
            instance.controller?.let {
                enc.update()
                it.currentState = MotionState(enc.pos, enc.vel)
                Logger.logInfo("set controller state to", it.currentState)
            }
        }

        return instance
    }
}
