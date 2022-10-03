package com.asiankoala.koawalib.hardware.sensor

import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.hardware.sensor.IMUUtil.remapAxes
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.math.d
import com.asiankoala.koawalib.util.NanoClock
import com.asiankoala.koawalib.util.Periodic
import com.qualcomm.hardware.bosch.BNO055IMU
import com.qualcomm.hardware.bosch.BNO055IMUImpl
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder
import org.firstinspires.ftc.robotcore.external.navigation.Orientation

/**
 * koawalib's IMU implementation with init offset and velocity calc for all 3 axis
 * Must call periodic() to poll the imu
 */
@Suppress("unused")
class KIMU(name: String, axesOrder: AxesOrder, axesSigns: AxesSigns) : KDevice<BNO055IMUImpl>(name), Periodic {
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
    var headingDelta = 0.0
        private set
    var pitchDelta = 0.0
        private set
    var rollDelta = 0.0
        private set
    var _headingVel = 0.0
        private set
    var _pitchVel = 0.0
        private set
    var _rollVel = 0.0
        private set

    val heading get() = (_heading - headingOffset).angleWrap
    val pitch get() = (_pitch - pitchOffset).angleWrap
    val roll get() = (_roll - rollOffset).angleWrap

    private var lastAngularOrientation: Orientation = device.angularOrientation
        private set(value) {
            _heading = value.firstAngle.d
            _pitch = value.secondAngle.d
            _roll = value.thirdAngle.d

            headingDelta = (_heading - lastHeading).angleWrap
            pitchDelta = (_pitch - lastPitch).angleWrap
            rollDelta = (_roll - lastRoll).angleWrap

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

    override fun periodic() {
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
