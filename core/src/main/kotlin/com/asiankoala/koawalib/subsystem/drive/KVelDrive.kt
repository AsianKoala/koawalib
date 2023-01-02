package com.asiankoala.koawalib.subsystem.drive

import com.asiankoala.koawalib.hardware.motor.KMotor
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.subsystem.odometry.Odometry
import kotlin.math.absoluteValue
import kotlin.math.sign

open class KVelDrive(
    fl: KMotor,
    bl: KMotor,
    br: KMotor,
    fr: KMotor,
    odometry: Odometry,
    shouldTelemetryOdo: Boolean,
    private val kP: Double,
    private val kS: Double,
    private val kV: Double,
    private val kA: Double,
    private var isVel: Boolean
) : KMecanumOdoDrive(fl, bl, br, fr, odometry, shouldTelemetryOdo) {
    private var setpoints = List(4) { Pair(0.0, 0.0) }
    private val vels get() = motors.map { it.vel }

    fun disableVel() {
        isVel = false
    }

    fun enableVel() {
        isVel = true
    }

    fun setSetpoint(vel: Pose, accel: Pose) {
        setpoints = mecKinematics(vel)
            .zip(mecKinematics(accel))
    }

    override fun periodic() {
        val wheels = if (isVel) {
            List(motors.size) { i ->
                kP * (setpoints[i].first - vels[i]) +
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
