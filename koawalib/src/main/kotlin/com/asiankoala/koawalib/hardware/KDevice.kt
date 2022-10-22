package com.asiankoala.koawalib.hardware

import com.asiankoala.koawalib.command.KScheduler
import com.qualcomm.robotcore.hardware.HardwareDevice
import com.qualcomm.robotcore.hardware.HardwareMap

/**
 * Hardware device that utilizes the hardware map
 * @param deviceName config name of device
 */
@Suppress("UNCHECKED_CAST")
abstract class KDevice<T : HardwareDevice>(val deviceName: String) {
    protected val device: T?
        get() {
            return if(!disabled) {
                hardwareMap[HardwareDevice::class.java as Class<T>, deviceName]
            } else {
                null
            }
        }
    internal var disabled = false

    override fun toString(): String {
        return deviceName
    }

    companion object {
        lateinit var hardwareMap: HardwareMap
    }
}
