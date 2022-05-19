package com.asiankoala.koawalib.hardware.motor

import com.acmerobotics.roadrunner.control.PIDFController
import com.asiankoala.koawalib.command.commands.LoopCmd
import com.asiankoala.koawalib.control.controller.ProfiledPIDController
import com.asiankoala.koawalib.control.profile.MotionConstraints
import com.asiankoala.koawalib.control.profile.MotionProfile
import com.asiankoala.koawalib.control.profile.MotionState
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.cos
import com.asiankoala.koawalib.subsystem.odometry.KEncoder
import com.qualcomm.robotcore.util.ElapsedTime
import kotlin.math.absoluteValue
import kotlin.math.sign

@Suppress("unused")
open class KMotorEx(
    val settings: KMotorExSettings,
) : KMotor(settings.name) {
    var output = 0.0; private set

    private val controller = PIDFController(
        settings.pid.coeffs,
        settings.ff.kV,
        settings.ff.kA,
        settings.ff.kS
    )
    val encoder = KEncoder(this, settings.ticksPerUnit, settings.isRevEncoder).zero(settings.startingPosition)

    private var targetState = MotionState()
    private var currentState = MotionState()

    private fun isInDisabledZone(): Boolean {
        // if we don't have a disabled position or still in motion
        if (settings.disabledPosition == null) return false
        // if our setpoint isn't in the deadzone
        if ((targetState.x - settings.disabledPosition!!).absoluteValue > settings.allowedPositionError) return false

        return isAtTarget(settings.disabledPosition!!)
    }

    fun isAtTarget(target: Double = targetState.x): Boolean {
        return (encoder.pos - target).absoluteValue < settings.allowedPositionError
    }

    fun setTarget(x: Double, v: Double = 0.0) {
        controller.reset()
        targetState = MotionState(x, v)
    }

    fun update() {
        encoder.update()

        controller.apply {
            targetPosition = targetState.x
            targetVelocity = targetState.v
            targetAcceleration = targetState.a
        }

        output = controller.update(encoder.pos, encoder.vel)
    }

    internal open fun applyMotorPower() {
        super.power = if(isInDisabledZone()) 0.0 else output
    }

    init {
        + LoopCmd({
            update()
            applyMotorPower()
        })
    }
}
