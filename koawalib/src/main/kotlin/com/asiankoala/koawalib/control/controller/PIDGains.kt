package com.asiankoala.koawalib.control.controller

import com.acmerobotics.roadrunner.control.PIDCoefficients

data class PIDGains(
    var kP: Double,
    var kI: Double,
    var kD: Double,
) {
    val coeffs get() = PIDCoefficients(kP, kI, kD)
}
