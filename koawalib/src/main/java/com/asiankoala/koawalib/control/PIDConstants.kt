package com.asiankoala.koawalib.control

import com.acmerobotics.roadrunner.control.PIDCoefficients

/**
 * Classic PID Constants
 * @param kP proportional term
 * @param kI integral term
 * @param kD derivative term
 */
data class PIDConstants(
    val kP: Double = 0.0,
    val kI: Double = 0.0,
    val kD: Double = 0.0,
) {
    val asCoeffs get() = PIDCoefficients(kP, kI, kD)
}