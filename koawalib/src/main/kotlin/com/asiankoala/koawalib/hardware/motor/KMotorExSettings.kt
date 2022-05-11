package com.asiankoala.koawalib.hardware.motor

import com.asiankoala.koawalib.control.motion.MotionConstraints

data class KMotorExSettings(
    var name: String,
    val ticksPerUnit: Double,
    val isRevEncoder: Boolean,
    var allowedPositionError: Double,
    var startingPosition: Double,
    var isMotionProfiled: Boolean,
    var isVoltageCorrected: Boolean,
    var pid: PIDSettings,
    var ff: FFSettings = FFSettings(),
    var constraints: MotionConstraints? = null,
    var disabledPosition: Double? = null,
    var disabledSettings: DisabledSettings = DisabledSettings()
)
