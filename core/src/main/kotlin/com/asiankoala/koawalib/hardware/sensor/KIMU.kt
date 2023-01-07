package com.asiankoala.koawalib.hardware.sensor

import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.hardware.sensor.IMUUtil.remapAxes
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.math.d
import com.asiankoala.koawalib.util.Clock
import com.asiankoala.koawalib.util.Periodic
import com.qualcomm.hardware.bosch.BNO055IMU
import com.qualcomm.hardware.bosch.BNO055IMUImpl
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder
import org.firstinspires.ftc.robotcore.external.navigation.Orientation

/**
 * IMU wrapper with offset functionality
 * Must call periodic() to poll the imu
 */
@Suppress("unused")
class KIMU(
    name: String,
    zOffset: Double,
    axesOrder: AxesOrder,
    axesSigns: AxesSigns
) : KDevice<BNO055IMUImpl>(name), Periodic {
    private val headingOffset: Double
    private val rollOffset: Double
    private val pitchOffset: Double

    private var lastHeading = 0.0
    private var lastPitch = 0.0
    private var lastRoll = 0.0

    val heading get() = (lastHeading - headingOffset).angleWrap
    val pitch get() = (lastPitch - pitchOffset).angleWrap
    val roll get() = (lastRoll - rollOffset).angleWrap

    var headingVel = 0.0; private set
    var pitchVel = 0.0; private set
    var rollVel = 0.0; private set

    override fun periodic() {
        val angles = device.angularOrientation
        val vels = device.angularVelocity
        lastHeading = angles.firstAngle.d
        lastPitch = angles.secondAngle.d
        lastRoll = angles.thirdAngle.d
        headingVel = vels.zRotationRate.d
        pitchVel = vels.xRotationRate.d
        rollVel = vels.yRotationRate.d
    }

    init {
        val parameters = BNO055IMU.Parameters()
        parameters.angleUnit = BNO055IMU.AngleUnit.RADIANS
        parameters.loggingEnabled = false
        device.initialize(parameters)
        remapAxes(device, axesOrder, axesSigns)

        val orientation = device.angularOrientation
        headingOffset = orientation.firstAngle.d + zOffset
        rollOffset = orientation.secondAngle.d
        pitchOffset = orientation.thirdAngle.d
    }
}
