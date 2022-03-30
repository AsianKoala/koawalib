package com.asiankoala.koawalib.hardware.sensor

import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.hardware.sensor.IMUUtil.remapAxes
import com.asiankoala.koawalib.math.*
import com.asiankoala.koawalib.util.Logger
import com.qualcomm.hardware.bosch.BNO055IMU
import com.qualcomm.hardware.bosch.BNO055IMUImpl
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder
import org.firstinspires.ftc.robotcore.external.navigation.Orientation

class KIMU(name: String, axesOrder: AxesOrder, axesSigns: AxesSigns) : KDevice<BNO055IMUImpl>(name) {
    private val headingOffset: Double
    private val rollOffset: Double
    private val pitchOffset: Double
    private var isReadFresh = false
    private var cancelTimer = ElapsedTime()
    private var canceling = false

    private var lastAngularOrientation: Orientation = device.angularOrientation
        private set(value) {
            heading = value.firstAngle.d
            pitch = value.secondAngle.d
            roll = value.thirdAngle.d
            field = value
        }

        get() {
            if (!isReadFresh) {
                if (!canceling) {
                    canceling = true
                    cancelTimer.reset()
                }

                Logger.logWarning("IMU not reading fresh updates")
//                Logger.logWarning("IMU not reading fresh updates")
//                Logger.logWarning("IMU not reading fresh updates")

//                if(cancelTimer.seconds() > 30) {
//                    Logger.logError("IMU failed")
//                }
            }
            isReadFresh = false
            return field
        }

    var heading = 0.0
    var pitch = 0.0
    var roll = 0.0

    fun periodic() {
        isReadFresh = true
        lastAngularOrientation = device.angularOrientation
//        Logger.addTelemetryData("last imu read", lastAngularOrientation.firstAngle)
//        Logger.addTelemetryData("last imu read 2", lastAngularOrientation.secondAngle)
//        Logger.addTelemetryData("last imu read 3", lastAngularOrientation.thirdAngle)
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
