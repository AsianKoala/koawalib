package com.asiankoala.koawalib.hardware

import com.qualcomm.robotcore.hardware.HardwareDevice
import com.qualcomm.robotcore.hardware.HardwareMap
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
abstract class KDevice<T : HardwareDevice>(name: String) {
    protected val device: T = hardwareMap[HardwareDevice::class.java as Class<T>, name]

    companion object {
        lateinit var hardwareMap: HardwareMap
    }
}
