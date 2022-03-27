package com.asiankoala.koawalib.hardware

import com.qualcomm.robotcore.hardware.HardwareDevice
import com.qualcomm.robotcore.hardware.HardwareMap

@Suppress("UNCHECKED_CAST")
abstract class KDevice<T : HardwareDevice>(protected val name: String) {
    protected val device: T = hardwareMap[HardwareDevice::class.java as Class<T>, name]

    companion object {
        lateinit var hardwareMap: HardwareMap
    }
}
