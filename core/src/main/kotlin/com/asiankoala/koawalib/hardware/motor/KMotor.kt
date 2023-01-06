package com.asiankoala.koawalib.hardware.motor

import com.asiankoala.koawalib.command.commands.LoopCmd
import com.asiankoala.koawalib.control.motor.MotionProfileMotorController
import com.asiankoala.koawalib.control.motor.MotorControlModes
import com.asiankoala.koawalib.control.motor.MotorController
import com.asiankoala.koawalib.control.profile.MotionState
import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.clamp
import com.asiankoala.koawalib.math.d
import com.asiankoala.koawalib.math.epsilonNotEqual
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import kotlin.math.absoluteValue
import kotlin.math.sign

@Suppress("unused")
class KMotor internal constructor(name: String) : KDevice<DcMotorEx>(name) {
    enum class Priority { HIGH, LOW }
    private val cmd = LoopCmd(this::update).withName("$name motor")
    private var powerMultiplier = 1.0
    private var disabled = false
    private var lastUpdateIter = 0
    internal lateinit var controller: MotorController
    internal var mode = MotorControlModes.OPEN_LOOP
    internal var encoderCreated = false
    internal var isVoltageCorrected = false
    internal val rawMotorPosition get() = device.currentPosition.d
    internal val rawMotorVelocity get() = device.velocity
    internal var priority = Priority.HIGH
    lateinit var encoder: KEncoder internal set
    internal var ks = 0.0

    val pos: Double get() = encoder.pos
    val vel: Double get() = encoder.vel

    internal var zeroPowerBehavior: DcMotor.ZeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
        set(value) {
            device.zeroPowerBehavior = value
            field = value
        }

    internal var direction: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD
        set(value) {
            powerMultiplier = if (value == DcMotorSimple.Direction.FORWARD) {
                1.0
            } else {
                -1.0
            }
            field = value
        }

    var power: Double = 0.0
        set(value) {
            val clamped = clamp(value * VOLTAGE_CONSTANT / lastVoltageRead, -1.0, 1.0) * powerMultiplier
            if (clamped epsilonNotEqual field &&
                (clamped == 0.0 || clamped.absoluteValue == 1.0 || (clamped - field).absoluteValue > 0.005) &&
                (priority == Priority.HIGH || iter - lastUpdateIter > 3)
            ) {
                field = clamped
                device.power = clamped + clamped.sign * ks
                lastUpdateIter = iter
            }
        }

    val setpoint: MotionState get() {
        if (mode != MotorControlModes.MOTION_PROFILE) {
            throw Exception("controller not motion profile controller")
        } else {
            return (controller as MotionProfileMotorController).setpoint
        }
    }

    val currState: MotionState get() {
        if (mode == MotorControlModes.OPEN_LOOP) {
            throw Exception("controller not closed loop")
        } else {
            return controller.currentState
        }
    }

    private fun update() {
        if (encoderCreated) encoder.update()
        if (mode == MotorControlModes.OPEN_LOOP) return
        controller.currentState = MotionState(encoder.pos, encoder.vel)
        controller.update()
        this.power = controller.output
        Logger.addTelemetryLine("updating motor controller for $deviceName")
    }

    fun setPositionTarget(x: Double) {
        if (mode != MotorControlModes.POSITION) throw Exception("motor must be position controlled")
        controller.setTargetPosition(x)
        Logger.logInfo("set motor $deviceName's position target to $x")
    }

    fun setVelocityTarget(v: Double) {
        if (mode != MotorControlModes.VELOCITY) throw Exception("motor must be velocity controlled")
        controller.setTargetVelocity(v)
    }

    fun setProfileTarget(x: Double) {
        if (mode != MotorControlModes.MOTION_PROFILE) throw Exception("motor must be motion profiled")
        controller.setProfileTarget(x)
    }

    fun isAtTarget(): Boolean {
        if (mode == MotorControlModes.OPEN_LOOP) throw Exception("motor must not be open loop")
        return controller.isAtTarget()
    }

    fun enable() {
        disabled = false
        power = 0.0
        if (encoderCreated) encoder.enable()
        cmd.schedule()
    }

    fun disable() {
        disabled = true
        power = 0.0
        if (encoderCreated) encoder.disable()
        cmd.cancel()
    }

    init {
        device.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        device.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
    }

    companion object {
        private var iter = 0
        private var VOLTAGE_CONSTANT = 12.0
        internal var lastVoltageRead = Double.NaN

        internal fun updatePriorityIter() {
            iter++
        }

        fun setVoltageConstant(x: Double) {
            VOLTAGE_CONSTANT = x
        }
    }
}
