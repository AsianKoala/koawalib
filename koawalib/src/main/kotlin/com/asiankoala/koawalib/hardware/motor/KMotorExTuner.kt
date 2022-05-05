package com.asiankoala.koawalib.hardware.motor

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.asiankoala.koawalib.command.KOpMode
import com.asiankoala.koawalib.command.commands.InstantCmd
import com.asiankoala.koawalib.gamepad.functionality.Button

/**
 * Steps
 * 1a. If kG or kCos don't need to be tuned, or have already been tuned, skip step 1
 * 1b. If kG needs to be tuned, initialize the opmode with the lift in the air (this will be the zero position)
 * Then increase kG until the lift can stay in the air without moving up or down
 * 1c. If kCos needs to be tuned, initialize the opmode with the arm at 90 degrees
 * Increase kCos until the arm stays at 90 degrees without moving.
 * An easy way to tune this is to hold the arm at 90 degrees, and then increase kCos until you feel near-zero force on your hand
 * 2. Stop and rerun the opmode with the lift at the real home position
 * 3. Make kV a very small positive number (ex: 0.00001)
 * 4. Increase kS until your lift barely starts to move. The goal of this is to find the minimum power to overcome friction
 * 5. Open FTC Dashboard and graph measured velocity and target velocity
 * 6. Increase kV until the measured velocity reaches the cruise state of the motion profile (the flat part)
 * 7. Increase kA until the measured velocity approximately resembles the slope of the acceleration/deceleration
 * 8. Now tune your PID gains with the PID Tuner. If you choose to change your motion constraints, you may need to revisit this opmode.
 */
@Config
@Suppress("unused")
abstract class KMotorExTuner : KOpMode() {
    abstract val motor: KMotorEx
    abstract val zeroPosition: Double
    abstract val toTargetButton: Button
    abstract val toHomeButton: Button
    abstract val homePosition: Double
    abstract val targetPosition: Double

    private val dashboard = FtcDashboard.getInstance()
    private var packet = TelemetryPacket()

    protected open fun updateMotorFFGains() {
        if (!kS.isNaN()) motor.settings.kS = kS else kS = motor.settings.kS
        if (!kV.isNaN()) motor.settings.kV = kV else kV = motor.settings.kV
        if (!kA.isNaN()) motor.settings.kA = kA else kA = motor.settings.kA
        if (!kG.isNaN()) motor.settings.kG = kG else kG = motor.settings.kG
        if (!kCos.isNaN()) motor.settings.kCos = kCos else kCos = motor.settings.kCos
    }

    override fun mInit() {
        motor.settings.isPIDEnabled = false
        motor.settings.isFFEnabled = true
        motor.encoder.zero(zeroPosition)
        updateMotorFFGains()
        toTargetButton.onPress(InstantCmd({ motor.setTarget(targetPosition) }))
        toHomeButton.onPress(InstantCmd({ motor.setTarget(homePosition) }))
    }

    override fun mLoop() {
        updateMotorFFGains()
        motor.update()
        packet.put("measured velocity", motor.encoder.vel)
        packet.put("target velocity", motor.setpointMotionState.v)
        dashboard.sendTelemetryPacket(packet)
        packet = TelemetryPacket()
    }

    companion object {
        @JvmField var kS: Double = Double.NaN
        @JvmField var kV: Double = Double.NaN
        @JvmField var kA: Double = Double.NaN
        @JvmField var kG: Double = Double.NaN
        @JvmField var kCos: Double = Double.NaN
    }
}