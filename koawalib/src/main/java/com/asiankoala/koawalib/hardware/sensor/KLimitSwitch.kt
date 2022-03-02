package com.asiankoala.koawalib.hardware.sensor

import com.asiankoala.koawalib.hardware.HardwareDevice
import com.asiankoala.koawalib.util.KBoolean
import com.qualcomm.robotcore.hardware.DigitalChannel

class KLimitSwitch : HardwareDevice<DigitalChannel>, KBoolean {
    constructor(device: DigitalChannel) : super(device)
    constructor(name: String) : super(name)

    override fun invokeBoolean(): Boolean {
        return device.state
    }
}
