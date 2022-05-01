package com.asiankoala.koawalib.rework

import com.asiankoala.koawalib.math.assertPositive

data class ComplexMotorSettings(
    var name: String,
    val _kP: Double,
    val _kI: Double,
    val _kD: Double,
    val ticksPerUnit: Double,
    val isRevEncoder: Boolean,
) {
    init {
        assertPositive(_kP)
        assertPositive(_kI)
        assertPositive(_kD)
        assertPositive(ticksPerUnit)
    }
}
