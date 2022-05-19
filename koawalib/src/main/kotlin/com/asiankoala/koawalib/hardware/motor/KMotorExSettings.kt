package com.asiankoala.koawalib.hardware.motor

import com.asiankoala.koawalib.control.controller.PIDGains
import com.asiankoala.koawalib.control.motor.MotorController
import com.asiankoala.koawalib.control.profile.MotionConstraints

data class KMotorExSettings(
    val name: String,
    val ticksPerUnit: Double,
    val isRevEncoder: Boolean,
    val controllerType: MotorController,
    var allowedPositionError: Double,
    var startingPosition: Double,
    var pid: PIDGains,
    var ff: FFSettings,
    var disabledPosition: Double? = null,
)
