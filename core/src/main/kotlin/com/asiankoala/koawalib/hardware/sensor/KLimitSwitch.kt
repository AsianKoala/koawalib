package com.asiankoala.koawalib.hardware.sensor

import com.asiankoala.koawalib.command.commands.Cmd
import com.asiankoala.koawalib.command.commands.LoopCmd
import com.asiankoala.koawalib.command.commands.WatchdogCmd
import com.asiankoala.koawalib.hardware.KDevice
import com.qualcomm.robotcore.hardware.DigitalChannel

class KLimitSwitch(name: String) : KDevice<DigitalChannel>(name), () -> Boolean {
    override fun invoke() = device.state
    fun onPress(cmd: Cmd) {
        + WatchdogCmd(cmd, ::invoke)
    }
}