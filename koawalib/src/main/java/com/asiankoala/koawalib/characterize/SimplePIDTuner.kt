package com.asiankoala.koawalib.characterize

import com.acmerobotics.dashboard.config.Config
import com.asiankoala.koawalib.command.KOpMode
import com.asiankoala.koawalib.command.commands.InstantCmd
import com.asiankoala.koawalib.control.PIDConstants
import com.asiankoala.koawalib.gamepad.functionality.Button
import com.asiankoala.koawalib.hardware.motor.KMotorEx

@Config
@Suppress("unused")
abstract class SimplePIDTuner() : KOpMode() {
    internal companion object {
        @JvmField var kP = Double.NaN
        @JvmField var kI = Double.NaN
        @JvmField var kD = Double.NaN
        @JvmField var kHome = Double.NaN
        @JvmField var kTarget = Double.NaN
    }

    abstract val motor: KMotorEx
    abstract val toHomeButton: Button
    abstract val toTargetButton: Button
    abstract val home: Double
    abstract val target: Double

    private var lastP = Double.NaN
    private var lastI = Double.NaN
    private var lastD = Double.NaN

    override fun mInit() {
        if (kHome.isNaN()) kHome = home
        if (kTarget.isNaN()) kTarget = target
        if (kP.isNaN()) {
            kP = motor.config.pid.kP
            kI = motor.config.pid.kI
            kD = motor.config.pid.kD
        } else {
            motor.setPIDConstants(PIDConstants(kP, kI, kD))
        }

        motor.setPIDTarget(home)
        toTargetButton.onPress(InstantCmd({ motor.setPIDTarget(target) }))
        toHomeButton.onPress(InstantCmd({ motor.setPIDTarget(home) }))
    }

    override fun mLoop() {
        if (lastP != kP || lastI != kI || lastD != kP) {
            motor.setPIDConstants(PIDConstants(kP, kI, kD))
        }

        lastP = kP
        lastI = kI
        lastD = kD
    }
}
