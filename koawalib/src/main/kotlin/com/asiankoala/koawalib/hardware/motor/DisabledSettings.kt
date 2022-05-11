package com.asiankoala.koawalib.hardware.motor

data class DisabledSettings(
    var isCompletelyDisabled: Boolean = false,
    var isPIDDisabled: Boolean = false,
    var isFFDisabled: Boolean = false,
)
