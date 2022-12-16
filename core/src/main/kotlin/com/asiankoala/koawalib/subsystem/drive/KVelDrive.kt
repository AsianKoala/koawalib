package com.asiankoala.koawalib.subsystem.drive

import com.asiankoala.koawalib.control.controller.ADRC
import com.asiankoala.koawalib.control.controller.ADRCConfig
import com.asiankoala.koawalib.hardware.motor.KMotor
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.subsystem.odometry.Odometry
import kotlin.math.absoluteValue
import kotlin.math.sign

class KVelDrive(
    fl: KMotor,
    bl: KMotor,
    br: KMotor,
    fr: KMotor,
    odometry: Odometry,
    shouldTelemetryOdo: Boolean,
    adrcConfig: ADRCConfig,
    private val kS: Double,
    private val kV: Double,
    private val kA: Double,
    private var isVel: Boolean
) : KMecanumOdoDrive(fl, bl, br, fr, odometry, shouldTelemetryOdo) {
    private val adrcs = motors.map { ADRC(adrcConfig) }
    private val us = List(4) { 0.0 }
    private var setpoints = List(4) { Pair(0.0, 0.0) }
    private val vels get() = motors.map { it.vel }

    fun disableVel() {
        isVel = false
    }

    fun enableVel() {
        isVel = true
    }

    fun setVelAccel(vel: Vector, accel: Vector) {
        setpoints = mecKinematics(Pose(vel, 0.0))
            .zip(mecKinematics(Pose(accel, 0.0)))
    }

    override fun periodic() {
        val wheels = if (isVel) {
            List(motors.size) { i ->
                adrcs[i].update(vels[i], us[i], setpoints[i].first) +
                    kS * setpoints[i].first.sign +
                    kV * setpoints[i].first +
                    kA * setpoints[i].second
            }
        } else mecKinematics(powers)
        val absMax = wheels.maxOf { it.absoluteValue }
        val scalar = if (absMax > 1.0) absMax else 1.0
        motors.forEachIndexed { i, it -> it.power = wheels[i] / scalar }
        updateOdo()
    }
}
