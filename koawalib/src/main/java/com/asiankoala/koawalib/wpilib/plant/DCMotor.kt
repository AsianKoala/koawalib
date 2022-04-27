// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.
package com.asiankoala.koawalib.wpilib.plant

/** Holds the constants for a DC motor.  */
@Suppress("unused")
internal class DCMotor(
    nominalVoltageVolts: Double,
    val stallTorqueNewtonMeters: Double,
    val stallCurrentAmps: Double,
    val freeCurrentAmps: Double,
    freeSpeedRadPerSec: Double,
) {
    val rOhms: Double = nominalVoltageVolts / this.stallCurrentAmps
    val KvRadPerSecPerVolt: Double = freeSpeedRadPerSec / (nominalVoltageVolts - rOhms * this.freeCurrentAmps)
    val KtNMPerAmp: Double = this.stallTorqueNewtonMeters / this.stallCurrentAmps

    /**
     * Estimate the current being drawn by this motor.
     *
     * @param speedRadiansPerSec The speed of the rotor.
     * @param voltageInputVolts The input voltage.
     * @return The estimated current.
     */
    fun getCurrent(speedRadiansPerSec: Double, voltageInputVolts: Double): Double {
        return -1.0 / KvRadPerSecPerVolt / rOhms * speedRadiansPerSec + 1.0 / rOhms * voltageInputVolts
    }
}
