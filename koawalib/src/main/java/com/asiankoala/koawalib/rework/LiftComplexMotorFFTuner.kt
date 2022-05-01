package com.asiankoala.koawalib.rework

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.asiankoala.koawalib.command.KOpMode
import com.asiankoala.koawalib.command.commands.InstantCmd
import com.asiankoala.koawalib.gamepad.functionality.Button

@Config
@Suppress("unused")
abstract class LiftComplexMotorFFTuner: KOpMode() {
    abstract val motor: LiftComplexMotor
    abstract val toTargetButton: Button
    abstract val toHomeButton: Button
    abstract val homePosition: Double
    abstract val targetPosition: Double

    private val dashboard = FtcDashboard.getInstance()
    private var packet = TelemetryPacket()

    private fun updateMotorFFGains() {
        if(!kG.isNaN()) motor.kG = kG
        if(!kS.isNaN()) motor.kS = kS
        if(!kV.isNaN()) motor.kV = kV
        if(!kA.isNaN()) motor.kA = kA
    }

    override fun mInit() {
        motor.isPIDEnabled = false
        motor.isFFEnabled = true
        updateMotorFFGains()

        toTargetButton.onPress(InstantCmd({motor.setTarget(targetPosition)}))
        toHomeButton.onPress(InstantCmd({motor.setTarget(homePosition)}))
    }

    override fun mLoop() {
        updateMotorFFGains()
        motor.update()
        packet.put("v", motor.encoder.vel)
        packet.put("target v", motor.setpointMotionState.v)
        dashboard.sendTelemetryPacket(packet)
        packet = TelemetryPacket()
    }

    internal companion object {
        @JvmField var kG: Double = Double.NaN
        @JvmField var kS: Double = Double.NaN
        @JvmField var kV: Double = Double.NaN
        @JvmField var kA: Double = Double.NaN
    }
}