package com.asiankoala.koawalib.hardware.servo

import com.asiankoala.koawalib.control.profile.v2.Constraints
import com.asiankoala.koawalib.control.profile.v2.DispState
import com.asiankoala.koawalib.control.profile.v2.OnlineProfile
import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.math.epsilonNotEqual
import com.asiankoala.koawalib.util.Periodic
import com.qualcomm.robotcore.hardware.Servo
import kotlin.math.absoluteValue

class KMPServo(
    name: String,
    private val constraints: Constraints,
    private val epsilon: Double
) : KDevice<Servo>(name), Periodic {
    private var position: Double = -1.0
        set(value) {
            if (value epsilonNotEqual field) {
                device.position = value
                field = value
            }
        }
    private var profile: OnlineProfile? = null
    private var target = 0.0

    fun setTarget(t: Double) {
        target = t
        profile = OnlineProfile(
            DispState(position),
            DispState(target),
            constraints
        )
    }

    override fun periodic() {
        profile?.let {
            if ((position - target).absoluteValue !in -epsilon..epsilon) {
                position = it[position].x
            }
        }
    }
}
