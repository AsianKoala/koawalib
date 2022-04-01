package com.asiankoala.koawalib.subsystem.old

import com.asiankoala.koawalib.command.commands.InstantCommand

@Suppress("unused")
class SpeedCommand(speed: Double, subsystem: MotorSubsystem)
    : InstantCommand({subsystem.setSpeedDirectly(speed)}, subsystem)