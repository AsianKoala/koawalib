package com.asiankoala.koawalib.hardware

import com.asiankoala.koawalib.command.KScheduler
import com.qualcomm.robotcore.hardware.HardwareDevice
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.hardware.VoltageSensor

/**
 * Hardware device that utilizes the hardware map
 * @param deviceName config name of device
 */
@Suppress("UNCHECKED_CAST")
abstract class KDevice<T : HardwareDevice>(val deviceName: String, test: Boolean = false) {
    protected val device: T = if(!test) hardwareMap[HardwareDevice::class.java as Class<T>, deviceName] else TestHardwareDevice() as T

    override fun toString(): String {
        return deviceName
    }

    init {
        KScheduler.registerDevices(this)
    }

    companion object {
        lateinit var hardwareMap: HardwareMap
        internal var lastVoltageRead = Double.NaN
        internal lateinit var voltageSensor: VoltageSensor
    }
}
