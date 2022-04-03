package com.asiankoala.koawalib.subsystem.old

import com.acmerobotics.roadrunner.control.PIDCoefficients

data class PIDConstants(
    val kP: Double = 0.0,
    val kI: Double = 0.0,
    val kD: Double = 0.0,
) {
    val asCoeffs get() = PIDCoefficients(kP, kI, kD)
}