package com.asiankoala.koawalib.hardware

import com.qualcomm.robotcore.hardware.HardwareDevice
import com.qualcomm.robotcore.hardware.HardwareMap

abstract class HardwareDevice<T : HardwareDevice>(protected val device: T) {
    constructor(name: String) : this(hardwareMap[HardwareDevice::class.java as Class<T>, name])

    companion object {
        lateinit var hardwareMap: HardwareMap
    }
}
