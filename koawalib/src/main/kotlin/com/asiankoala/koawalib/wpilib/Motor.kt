// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.
package com.asiankoala.koawalib.wpilib

/** Holds the constants for a DC motor.  */
@Suppress("unused")
internal class Motor(
    val nominalVoltageVolts: Double,
    val stallTorqueNewtonMeters: Double,
    val stallCurrentAmps: Double,
    val freeCurrentAmps: Double,
    val freeSpeedRadPerSec: Double,
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

    companion object {
        private fun createGoMotor(kgCm: Double, rpm: Double) = Motor(
            12.0, Units.kgCmToNewtonMeters(kgCm), 9.2, 0.25, Units.rotationsPerMinuteToRadiansPerSecond(rpm)
        )

        fun createGo6000() = createGoMotor(1.47, 5400.0)
        fun createGo1620() = createGoMotor(5.4, 1620.0)
        fun createGo1150() = createGoMotor(7.9, 1150.0)
        fun createGo435() = createGoMotor(18.7, 435.0)
        fun createGo312() = createGoMotor(24.3, 312.0)
    }
}
