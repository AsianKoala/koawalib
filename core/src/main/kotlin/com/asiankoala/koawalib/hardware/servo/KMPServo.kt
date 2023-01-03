package com.asiankoala.koawalib.hardware.servo

import com.asiankoala.koawalib.control.profile.MotionConstraints
import com.asiankoala.koawalib.control.profile.MotionProfile
import com.asiankoala.koawalib.control.profile.MotionState
import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.math.epsilonNotEqual
import com.asiankoala.koawalib.util.Periodic
import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.util.ElapsedTime
import kotlin.math.absoluteValue

class KMPServo(
    name: String,
    private val constraints: MotionConstraints,
    private val epsilon: Double = 0.01
) : KDevice<Servo>(name), Periodic {
    private var direction: Servo.Direction = Servo.Direction.FORWARD
    private var target = -1.0
    private var profile: MotionProfile? = null
    private val timer = ElapsedTime()
    private var setpoint: Double = -1.0
        set(value) {
            if (value epsilonNotEqual field) {
                device.position = value
                field = value
            }
        }

    val isAtTarget get() = (target - setpoint).absoluteValue < epsilon
            && timer.seconds() > (profile?.duration ?: -1.0)

    fun startAt(startPos: Double): KMPServo {
        setpoint = startPos
        target = startPos
        return this
    }

    val reverse: KMPServo
        get() {
            direction = Servo.Direction.REVERSE
            return this
        }

    fun setTarget(x: Double) {
        target = x
        profile = MotionProfile.generateTrapezoidal(
            MotionState(setpoint),
            MotionState(target),
            constraints
        )
        timer.reset()
    }

    override fun periodic() {
        if(!isAtTarget) {
            profile?.let {
                setpoint = it[timer.seconds()].x
            }
        }
    }
}