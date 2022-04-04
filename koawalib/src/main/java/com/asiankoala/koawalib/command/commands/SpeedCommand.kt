package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.subsystem.old.MotorSubsystem

@Suppress("unused")
class SpeedCommand(speed: Double, subsystem: MotorSubsystem)
    : InstantCommand({subsystem.setSpeedDirectly(speed)}, subsystem)