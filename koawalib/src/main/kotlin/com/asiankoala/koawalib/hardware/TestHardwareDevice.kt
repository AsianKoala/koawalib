package com.asiankoala.koawalib.hardware

import com.qualcomm.robotcore.hardware.HardwareDevice

internal class TestHardwareDevice : HardwareDevice {
    override fun getManufacturer(): HardwareDevice.Manufacturer =
        HardwareDevice.Manufacturer.Unknown

    override fun getDeviceName(): String = ""

    override fun getConnectionInfo(): String = ""

    override fun getVersion(): Int = -1

    override fun resetDeviceConfigurationForOpMode() {}

    override fun close() {}
}

internal class TestDevice(name: String) : KDevice<TestHardwareDevice>(name, test = true)