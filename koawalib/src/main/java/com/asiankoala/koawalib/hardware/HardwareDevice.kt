package com.asiankoala.koawalib.hardware

import com.qualcomm.robotcore.hardware.HardwareDevice
import com.qualcomm.robotcore.hardware.HardwareMap

abstract class HardwareDevice<T : HardwareDevice>(private val name: String) {
    protected val device: T = hardwareMap[HardwareDevice::class.java as Class<T>, name]

    companion object {
        lateinit var hardwareMap: HardwareMap
    }
}
