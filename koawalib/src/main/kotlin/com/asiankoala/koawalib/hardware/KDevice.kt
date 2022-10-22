package com.asiankoala.koawalib.hardware

import com.qualcomm.robotcore.hardware.HardwareDevice
import com.qualcomm.robotcore.hardware.HardwareMap

/**
 * Hardware device that utilizes the hardware map
 * @param deviceName config name of device
 */
@Suppress("UNCHECKED_CAST")
abstract class KDevice<T : HardwareDevice>(val deviceName: String) {
    protected val device: T = hardwareMap[HardwareDevice::class.java as Class<T>, deviceName]

    override fun toString(): String {
        return deviceName
    }

    companion object {
        lateinit var hardwareMap: HardwareMap
    }
}
