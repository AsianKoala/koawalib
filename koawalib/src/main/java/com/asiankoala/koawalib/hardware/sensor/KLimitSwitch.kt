package com.asiankoala.koawalib.hardware.sensor

import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.util.KBoolean
import com.qualcomm.robotcore.hardware.DigitalChannel

@Suppress("unused")
class KLimitSwitch(name: String) : KDevice<DigitalChannel>(name), KBoolean {

    override fun invokeBoolean(): Boolean {
        return device.state
    }
}
