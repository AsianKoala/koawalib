package com.asiankoala.koawalib.control.motion

import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sqrt

private class MotionProfile(private val target: Double, private val constraints: ProfileConstraints) {
    private var accelTime: Double
    private var cruiseTime: Double
    private var deccelTime: Double
    private val totalTime: Double

    operator fun get(time: Double): ProfileState {
        return when {
            time <= accelTime -> {
                val a = constraints.maxAcceleration.absoluteValue
                val v = time * a
                val x = 0.5 * a * time * time
                ProfileState(x, v, a)
            }

            time <= accelTime + cruiseTime -> {
                val a = 0.0
                val v = get(accelTime).v
                val x = get(accelTime).x + constraints.maxVelocity * (time - accelTime)
                ProfileState(x, v, a)
            }

            time <= totalTime -> {
                var a = constraints.maxDeceleration.absoluteValue
                val coastV = get(accelTime).v
                val v = coastV- (time - (accelTime + cruiseTime)) * a
                val endOfCruise = accelTime + cruiseTime
                val endOfCruisePos = get(endOfCruise).x
                val x = endOfCruisePos + coastV * (time - endOfCruise) - 0.5 * a * (time - endOfCruise).pow(2)
                a *= -1
                ProfileState(x, v, a)
            }
            else -> ProfileState(target, 0.0, 0.0)
        }
    }

    init {
        accelTime = (constraints.maxVelocity / constraints.maxAcceleration).absoluteValue
        deccelTime = (constraints.maxVelocity / constraints.maxDeceleration).absoluteValue
        val avgAccelTime = (accelTime + deccelTime) / 2
        cruiseTime = target.absoluteValue / constraints.maxVelocity - avgAccelTime

        if(cruiseTime < 0) {

            cruiseTime = 0.0
            if(constraints.maxAcceleration.absoluteValue > constraints.maxDeceleration.absoluteValue) {
                constraints.maxAcceleration = constraints.maxDeceleration.absoluteValue
            } else {
                constraints.maxDeceleration = constraints.maxAcceleration.absoluteValue
            }

            accelTime = sqrt((target / constraints.maxAcceleration).absoluteValue)
            deccelTime = sqrt((target / constraints.maxDeceleration).absoluteValue)
        }
        totalTime = accelTime + cruiseTime + deccelTime
    }
}