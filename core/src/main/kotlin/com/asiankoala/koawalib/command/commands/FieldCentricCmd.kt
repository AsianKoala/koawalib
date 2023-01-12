package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.gamepad.KStick
import com.asiankoala.koawalib.hardware.sensor.KIMU
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.subsystem.drive.KMecanumDrive

class FieldCentricCmd @JvmOverloads constructor(
    private val imu: KIMU,
    drive: KMecanumDrive,
    leftStick: KStick,
    rightStick: KStick,
    scalars: Pose = Pose(1.0, 1.0, 1.0),
    cubics: Pose = Pose(1.0, 1.0, 1.0),
    constants: Pose = Pose(1.0, 1.0, 1.0),
) : MecanumCmd(drive, leftStick, rightStick, scalars, cubics, constants) {
    override fun execute() {
        val powers = processPowers()
        val rot = powers.vec.rotate(-imu.heading)
        drive.powers = Pose(rot, powers.heading)
    }
}
