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
import com.asiankoala.koawalib.subsystem.odometry.KEncoder
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.util.Range
import kotlin.math.absoluteValue

/**
 * The koawalib standard open-loop motor. Default settings are zeroPowerBehavior: float and direction: forward
 * @see KMotorEx for closed-loop control
 */
open class KMotor(name: String) : KDevice<DcMotorEx>(name) {
    private var powerMultiplier = 1.0

    internal var isVoltageCorrected = false
        private set

    // overall control
    private var mode = MotorControlModes.OPEN_LOOP
    lateinit var encoder: KEncoder; private set
    private lateinit var controller: MotorController

    /**
     * raw motor position (ticks, no offset)
     */
    internal val rawMotorPosition get() = device.currentPosition.d

    /**
     * raw motor velocity (ticks)
     */
    internal val rawMotorVelocity get() = device.velocity

    internal fun update() {
        if(mode == MotorControlModes.OPEN_LOOP) return

        controller.updateEncoder()
        controller.update()

        var rawOutput = controller.output

        if(isVoltageCorrected) {
            rawOutput *= (12.0 / lastVoltageRead)
        }

        this.power = rawOutput
    }

    var power: Double = 0.0
        set(value) {
            var clipped = Range.clip(value, -1.0, 1.0) * powerMultiplier
            if(isVoltageCorrected) clipped *= (12.0 / lastVoltageRead)
            if (clipped epsilonNotEqual field && (clipped == 0.0 || clipped.absoluteValue == 1.0 || (clipped - field).absoluteValue > 0.005)) {
                field = clipped
                device.power = clipped
            }
        }

    var zeroPowerBehavior: DcMotor.ZeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
        private set(value) {
            device.zeroPowerBehavior = value
            field = value
        }

    var direction: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD
        private set(value) {
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

    fun withPositionControl(ticksPerUnit: Double, isRevEncoder: Boolean, pidGains: PIDGains, ffGains: FFGains,
                       allowedPositionError: Double, disabledPosition: DisabledPosition = DisabledPosition.NONE): KMotor {
        mode = MotorControlModes.POSITION
        encoder = KEncoder(this, ticksPerUnit, isRevEncoder)
        controller = PositionMotorController(encoder, pidGains, ffGains, allowedPositionError, disabledPosition)
        return this
    }

    fun withVelocityControl(ticksPerUnit: Double, isRevEncoder: Boolean,
                       pidGains: PIDGains, kF: Double, allowedVelocityError: Double): KMotor {
        mode = MotorControlModes.VELOCITY
        encoder = KEncoder(this, ticksPerUnit, isRevEncoder)
        controller = VelocityMotorController(encoder, pidGains, kF, allowedVelocityError)
        return this
    }

    fun withMotionProfileControl(ticksPerUnit: Double, isRevEncoder: Boolean, pidGains: PIDGains, ffGains: FFGains, constraints: MotionConstraints,
                                 allowedPositionError: Double, disabledPosition: DisabledPosition = DisabledPosition.NONE): KMotor {
        mode = MotorControlModes.MOTION_PROFILE
        encoder = KEncoder(this, ticksPerUnit, isRevEncoder)
        controller = MotionProfileMotorController(encoder, pidGains, ffGains, constraints, allowedPositionError, disabledPosition)
        return this
    }

    fun setTargetPosition(x: Double) {
        if(mode != MotorControlModes.POSITION) throw Exception("motor must be position controlled")
        controller.setTargetPosition(x)
    }

    fun setTargetVelocity(v: Double) {
        if(mode != MotorControlModes.VELOCITY) throw Exception("motor must be velocity controlled")
        controller.setTargetVelocity(v)
    }

    fun setProfileTarget(x: Double, v: Double = 0.0) {
        if(mode != MotorControlModes.MOTION_PROFILE) throw Exception("motor must be motion profiled")
        controller.setProfileTarget(x, v)
    }

    fun isAtTarget(): Boolean {
        if(mode == MotorControlModes.OPEN_LOOP) throw Exception("motor must not be open loop")
        return controller.isAtTarget()
    }

    fun forceScheduleUpdate() {
        + LoopCmd(this::update)
    }

    init {
        device.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        device.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT

        if(mode != MotorControlModes.OPEN_LOOP) {
            forceScheduleUpdate()
        }
    }
}
