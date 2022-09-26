package com.asiankoala.koawalib.hardware.motor

import com.acmerobotics.dashboard.config.Config
import com.asiankoala.koawalib.command.KOpMode

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
//@Config
@Suppress("unused")
@Deprecated("unusable for now with KMotorEx removal")
internal abstract class KMotorExTuner : KOpMode() {
//    abstract val motor: KMotor
//    abstract val zeroPosition: Double
//    abstract val toTargetButton: Button
//    abstract val toHomeButton: Button
//    abstract val homePosition: Double
//    abstract val targetPosition: Double
//
//    private val dashboard = FtcDashboard.getInstance()
//    private var packet = TelemetryPacket()
//
//    protected open fun updateMotorFFGains() {
//        if (!kS.isNaN()) motor.settings.ff.kS = kS else kS = motor.settings.ff.kS
//        if (!kV.isNaN()) motor.settings.ff.kV = kV else kV = motor.settings.ff.kV
//        if (!kA.isNaN()) motor.settings.ff.kA = kA else kA = motor.settings.ff.kA
//        if (!kG.isNaN()) motor.settings.ff.kG = kG else kG = motor.settings.ff.kG
// //        if (!kCos.isNaN()) motor.settings.ff.kCos = kCos else kCos = motor.settings.ff.kCos
//        if (!kP.isNaN()) motor.settings.pid.kP = kP else kP = motor.settings.pid.kP
//        if (!kI.isNaN()) motor.settings.pid.kI = kI else kI = motor.settings.pid.kI //        if (!kD.isNaN()) motor.settings.pid.kD = kD else kD = motor.settings.pid.kD
//    }
//
//    override fun mInit() {
//        motor.settings.disabledSettings.isPIDDisabled = true
//        motor.settings.disabledSettings.isFFDisabled = false
//        motor.encoder.zero(zeroPosition)
//        updateMotorFFGains()
//        toTargetButton.onPress(InstantCmd({ motor.setTarget(targetPosition) }))
//        toHomeButton.onPress(InstantCmd({ motor.setTarget(homePosition) }))
//    }
//
//    override fun mLoop() {
//        updateMotorFFGains()
//        motor.update()
//        packet.put("measured velocity", motor.encoder.vel)
//        packet.put("target velocity", motor.setpointMotionState.v)
//        dashboard.sendTelemetryPacket(packet)
//        packet = TelemetryPacket()
//    }
//
//    companion object {
//        @JvmField var kS: Double = Double.NaN
//        @JvmField var kV: Double = Double.NaN
//        @JvmField var kA: Double = Double.NaN
//        @JvmField var kG: Double = Double.NaN
//        @JvmField var kCos: Double = Double.NaN
//        @JvmField var kP: Double = Double.NaN
//        @JvmField var kI: Double = Double.NaN
//        @JvmField var kD: Double = Double.NaN
//    }
}
