package com.asiankoala.koawalib.hardware.motor

import com.asiankoala.koawalib.command.commands.LoopCmd
import com.asiankoala.koawalib.control.controller.PIDGains
import com.asiankoala.koawalib.control.motor.*
import com.asiankoala.koawalib.control.motor.MotorController
import com.asiankoala.koawalib.control.motor.PositionMotorController
import com.asiankoala.koawalib.control.motor.VelocityMotorController
import com.asiankoala.koawalib.control.profile.MotionConstraints
import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.math.d
import com.asiankoala.koawalib.math.epsilonNotEqual
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.util.Range
import kotlin.math.absoluteValue

/**
 * The koawalib standard open-loop motor. Default settings are zeroPowerBehavior: float and direction: forward
 * @see KMotorEx for closed-loop control
 * todo: when first motor command is called, refresh motor encoder
 */
class KMotor(name: String) : KDevice<DcMotorEx>(name) {
    private var powerMultiplier = 1.0
    private var disabled = false
    internal var isVoltageCorrected = false; private set

    private var mode = MotorControlModes.OPEN_LOOP
    private lateinit var controller: MotorController
    private var encoderCreated = false
    private lateinit var encoder: KEncoder; private set

    internal val rawMotorPosition get() = device.currentPosition.d
    internal val rawMotorVelocity get() = device.velocity

    private val cmd = LoopCmd(this::update).withName("$name motor")

    private fun update() {
        if (mode == MotorControlModes.OPEN_LOOP) return

        controller.updateEncoder()
        controller.update()

        var rawOutput = controller.output

        if (isVoltageCorrected) {
            rawOutput *= (12.0 / lastVoltageRead)
        }

        this.power = rawOutput
    }

    var power: Double = 0.0
        set(value) {
            var clipped = Range.clip(value, -1.0, 1.0) * powerMultiplier
            if (isVoltageCorrected) clipped *= (12.0 / lastVoltageRead)
            if (clipped epsilonNotEqual field && (clipped == 0.0 || clipped.absoluteValue == 1.0 || (clipped - field).absoluteValue > 0.005)) {
                field = clipped
                device.power = clipped
            }
        }

    private var zeroPowerBehavior: DcMotor.ZeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
        set(value) {
            device.zeroPowerBehavior = value
            field = value
        }

    private var direction: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD
        set(value) {
            powerMultiplier = if (value == DcMotorSimple.Direction.FORWARD) {
                1.0
            } else {
                -1.0
            }
            field = value
        }

    /**
     * Return this motor with brake mode
     */
    val brake: KMotor
        get() {
            zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            return this
        }

    /**
     * Return this motor with float mode
     */
    val float: KMotor
        get() {
            zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
            return this
        }

    /**
     * Return this motor with direction forward
     */
    val forward: KMotor
        get() {
            direction = DcMotorSimple.Direction.FORWARD
            return this
        }

    /**
     * Return this motor with direction backward
     */
    val reverse: KMotor
        get() {
            direction = DcMotorSimple.Direction.REVERSE
            return this
        }

    fun createEncoder(ticksPerUnit: Double, isRevEncoder: Boolean): KMotor {
        encoder = KEncoder(this, ticksPerUnit, isRevEncoder)
        encoderCreated = true
        return this
    }

    fun pairEncoder(toPair: KEncoder): KMotor {
        encoder = toPair
        encoderCreated = true
        return this
    }

    fun zero(newPosition: Double = 0.0): KMotor {
        if (!encoderCreated) throw Exception("encoder has not been created yet")
        encoder.zero(newPosition)
        return this
    }

    val reverseEncoder: KMotor
        get() {
            encoder.reverse
            return this
        }

    val pos: Double
        get() {
            if (!encoderCreated) throw Exception("encoder has not been created yet")
            return encoder.pos
        }

    val vel: Double
        get() {
            if (!encoderCreated) throw Exception("encoder has not been created yet")
            return encoder.vel
        }

    val accel: Double
        get() {
            if (!encoderCreated) throw Exception("encoder has not been created yet")
            return encoder.accel
        }

    fun withPositionControl(
        pidGains: PIDGains,
        ffGains: FFGains,
        allowedPositionError: Double,
        disabledPosition: DisabledPosition = DisabledPosition.NONE
    ): KMotor {
        mode = MotorControlModes.POSITION
        controller = PositionMotorController(encoder, pidGains, ffGains, allowedPositionError, disabledPosition)
        return this
    }

    fun withVelocityControl(
        pidGains: PIDGains,
        kF: Double,
        allowedVelocityError: Double
    ): KMotor {
        mode = MotorControlModes.VELOCITY
        controller = VelocityMotorController(encoder, pidGains, kF, allowedVelocityError)
        return this
    }

    fun withMotionProfileControl(
        pidGains: PIDGains,
        ffGains: FFGains,
        constraints: MotionConstraints,
        allowedPositionError: Double,
        disabledPosition: DisabledPosition = DisabledPosition.NONE
    ): KMotor {
        mode = MotorControlModes.MOTION_PROFILE
        controller = MotionProfileMotorController(encoder, pidGains, ffGains, constraints, allowedPositionError, disabledPosition)
        return this
    }

    fun setPositionTarget(x: Double) {
        if (mode != MotorControlModes.POSITION) throw Exception("motor must be position controlled")
        controller.setTargetPosition(x)
    }

    fun setVelocityTarget(v: Double) {
        if (mode != MotorControlModes.VELOCITY) throw Exception("motor must be velocity controlled")
        controller.setTargetVelocity(v)
    }

    fun setProfileTarget(x: Double, v: Double = 0.0) {
        if (mode != MotorControlModes.MOTION_PROFILE) throw Exception("motor must be motion profiled")
        controller.setProfileTarget(x, v)
    }

    fun isAtTarget(): Boolean {
        if (mode == MotorControlModes.OPEN_LOOP) throw Exception("motor must not be open loop")
        return controller.isAtTarget()
    }

    fun enable() {
        power = 0.0
        disabled = false
        if (mode != MotorControlModes.OPEN_LOOP) {
            encoder.enable()
        }
    }

    fun disable() {
        power = 0.0
        disabled = true
        if (mode != MotorControlModes.OPEN_LOOP) {
            encoder.disable()
        }
    }

    fun schedule() {
        + cmd
    }

    fun cancel() {
        - cmd
    }

    init {
        device.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        device.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT

        if (mode != MotorControlModes.OPEN_LOOP) {
            schedule()
        }
    }
}
