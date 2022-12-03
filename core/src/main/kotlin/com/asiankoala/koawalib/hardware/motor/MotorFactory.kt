package com.asiankoala.koawalib.hardware.motor

import com.asiankoala.koawalib.control.controller.Bounds
import com.asiankoala.koawalib.control.controller.PIDGains
import com.asiankoala.koawalib.control.motor.*
import com.asiankoala.koawalib.control.profile.MotionConstraints
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

    val lowPriority: MotorFactory
        get() {
            instance.priority = KMotor.Priority.LOW
            return this
        }

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
     * Enable position PID control in the motor
     */
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
            allowedPositionError,
            disabledPosition?.let { DisabledPosition(it) } ?: DisabledPosition.NONE,
            bounds
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
    fun withMotionProfileControl(
        pidGains: PIDGains,
        ffGains: FFGains,
        constraints: MotionConstraints,
        allowedPositionError: Double,
        disabledPosition: Double? = null,
    ): MotorFactory {
        instance.mode = MotorControlModes.MOTION_PROFILE
        instance.controller = MotionProfileMotorController(
            pidGains,
            ffGains,
            constraints,
            allowedPositionError,
            disabledPosition?.let { DisabledPosition(it) } ?: DisabledPosition.NONE,
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
            "zeroPowerBehavior: ${instance.zeroPowerBehavior}\n" +
            "isVoltageCorrected: ${instance.isVoltageCorrected}\n"

        Logger.logInfo("scheduled motor with information: $information")
        return instance
    }
}
