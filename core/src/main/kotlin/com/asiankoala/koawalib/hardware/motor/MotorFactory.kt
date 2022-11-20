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
     * Return this motor with brake mode
     */
    val brake: MotorFactory
        get() {
            instance.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            return this
        }

    /**
     * Return this motor with float mode
     */
    val float: MotorFactory
        get() {
            instance.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
            return this
        }

    /**
     * Return this motor with direction forward
     */
    val forward: MotorFactory
        get() {
            instance.direction = DcMotorSimple.Direction.FORWARD
            return this
        }

    /**
     * Return this motor with direction reversed
     */
    val reverse: MotorFactory
        get() {
            instance.direction = DcMotorSimple.Direction.REVERSE
            return this
        }

    /**
     * Return this motor with voltage correction
     */
    val voltageCorrected: MotorFactory
        get() {
            instance.isVoltageCorrected = true
            return this
        }


    fun pairEncoder(
        motor: KMotor,
        ticksPerUnit: Double,
        isRevEncoder: Boolean = false)
    : MotorFactory {
        instance.encoder = KEncoder(motor, ticksPerUnit, isRevEncoder)
        instance.encoderCreated = true
        Logger.logInfo("encoder for motor ${instance.deviceName} paired with encoder on motor ${motor.deviceName}'s port")
        return this
    }

    /**
     * Created an encoder association with this motor
     */
    fun createEncoder(
        ticksPerUnit: Double,
        isRevEncoder: Boolean = false
    ) = pairEncoder(instance, ticksPerUnit, isRevEncoder)


    /**
     * Zero the encoder associated with this motor
     */
    fun zero(newPosition: Double = 0.0): MotorFactory {
        if (!instance.encoderCreated) throw Exception("encoder has not been created yet")
        instance.encoder.zero(newPosition)
        return this
    }

    /**
     * Reverse the encoder associated with this motor
     */
    val reverseEncoder: MotorFactory
        get() {
            if (!instance.encoderCreated) throw Exception("encoder has not been created yet")
            instance.encoder.reverse
            return this
        }

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
