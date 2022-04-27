package com.asiankoala.koawalib.characterize

import com.asiankoala.koawalib.command.KOpMode
import com.asiankoala.koawalib.command.commands.InstantCmd
import com.asiankoala.koawalib.gamepad.KButton
import com.asiankoala.koawalib.hardware.motor.KMotorEx

class SimplePIDTuner(
    private val motor: KMotorEx,
    private val home: Double,
    private val target: Double,
    private val button: KButton
) : KOpMode() {
    override fun mInit() {
        motor.setPIDTarget(home)
        button.onPress(InstantCmd({motor.setPIDTarget(target)}).waitUntil {  })
    }
}