package com.asiankoala.koawalib.hardware.sensor

import com.acmerobotics.roadrunner.util.NanoClock
import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.hardware.sensor.IMUUtil.remapAxes
import com.asiankoala.koawalib.math.*
import com.asiankoala.koawalib.util.Logger
import com.qualcomm.hardware.bosch.BNO055IMU
import com.qualcomm.hardware.bosch.BNO055IMUImpl
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder
import org.firstinspires.ftc.robotcore.external.navigation.Orientation

@Suppress("unused")
class KIMU(name: String, axesOrder: AxesOrder, axesSigns: AxesSigns) : KDevice<BNO055IMUImpl>(name) {
    private val headingOffset: Double
    private val rollOffset: Double
    private val pitchOffset: Double

    private var isReadFresh = false
    private var clock = NanoClock.system()
    private var lastUpdateTime = clock.seconds()
    private var lastHeading = 0.0
    private var lastPitch = 0.0
    private var lastRoll = 0.0
    private var _heading = 0.0
    private var _pitch = 0.0
    private var _roll = 0.0
    private var _headingVel = 0.0
    private var _pitchVel = 0.0
    private var _rollVel = 0.0

    val heading get() = _heading
    val pitch get() = _pitch
    val roll get() = _roll
    val headingVel get() = _headingVel
    val pitchVel get() = _pitchVel
    val rollVel get() = _rollVel


    private var lastAngularOrientation: Orientation = device.angularOrientation
        private set(value) {
            _heading = value.firstAngle.d
            _pitch = value.secondAngle.d
            _roll = value.thirdAngle.d

            val currTime = clock.seconds()
            val dt = currTime - lastUpdateTime
            _headingVel = (_heading - lastHeading) / dt
            _pitchVel = (_pitch - lastPitch) / dt
            _rollVel = (_roll - lastRoll) / dt

            lastUpdateTime = currTime
            lastHeading = _heading
            lastPitch = _pitch
            lastRoll = _roll

            field = value
        }

        get() {
            if (!isReadFresh) {
                Logger.logWarning("IMU not reading fresh updates")
            }
            isReadFresh = false
            return field
        }


    fun periodic() {
        isReadFresh = true
        lastAngularOrientation = device.angularOrientation
    }

    init {
        val parameters = BNO055IMU.Parameters()
        parameters.angleUnit = BNO055IMU.AngleUnit.RADIANS
        parameters.loggingEnabled = false
        device.initialize(parameters)
        remapAxes(device, axesOrder, axesSigns)

        val orientation = device.angularOrientation
        headingOffset = orientation.firstAngle.d
        rollOffset = orientation.secondAngle.d
        pitchOffset = orientation.thirdAngle.d
    }
}
