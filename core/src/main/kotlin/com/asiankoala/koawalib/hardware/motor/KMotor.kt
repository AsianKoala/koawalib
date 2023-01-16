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

//@Suppress("unused")
class KMotor internal constructor(name: String) : KDevice<DcMotorEx>(name) {
    enum class Priority { HIGH, LOW }
    private val cmd = LoopCmd(this::update).withName("$name motor")
    private var powerMultiplier = 1.0
    private var lastUpdateIter = 0
    internal var controller: MotorController? = null
    internal var encoder: KEncoder? = null
    internal var mode = MotorControlModes.OPEN_LOOP
    internal var encoderCreated = false
    internal var isVoltageCorrected = false
    internal val rawMotorPosition get() = device.currentPosition.d
    internal val rawMotorVelocity get() = device.velocity
    internal var priority = Priority.HIGH
    internal var ks = 0.0

    val pos: Double get() = encoder?.pos ?: throw Exception("queried motor $deviceName's pos without paired encoder")
    val vel: Double get() = encoder?.vel ?: throw Exception("queried motor $deviceName's vel without paired encoder")

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
        if(controller !is MotionProfileMotorController) throw Exception("controller not motion profile controller")
        return (controller as MotionProfileMotorController).setpoint
    }

    val currState: MotionState get() = controller?.currentState ?: throw Exception("fuck")

    private fun update() {
        encoder?.let { enc ->
            controller?.let {
                it.currentState = MotionState(enc.pos, enc.vel)
                it.update()
                power = it.output
                Logger.addTelemetryLine("updating motor controller for $deviceName")
            }
        }
    }

    fun setPositionTarget(x: Double) {
        controller?.setTargetPosition(x) ?: throw Exception("motor must be position controlled")
        Logger.logInfo("set motor $deviceName's position target to $x")
    }

    fun setVelocityTarget(v: Double) {
        controller?.setTargetVelocity(v) ?: throw Exception("motor must be velocity controlled")
    }

    fun setProfileTarget(x: Double) {
        controller?.setProfileTarget(x) ?: throw Exception("motor must be motion profiled")
    }

    fun isAtTarget() = controller?.isAtTarget() ?: throw Exception("motor must not be open loop")

    fun enable() {
        power = 0.0
        cmd.schedule()
    }

    fun disable() {
        power = 0.0
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
