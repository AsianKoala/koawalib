package com.asiankoala.koawalib.control

import com.acmerobotics.roadrunner.profile.MotionProfile
import com.acmerobotics.roadrunner.profile.MotionProfileGenerator
import com.acmerobotics.roadrunner.profile.MotionState
import com.qualcomm.robotcore.util.ElapsedTime

class MotionProfileController(private val config: MotionProfileConfig) : PIDExController(config.pidConfig) {
    private var motionTimer = ElapsedTime()
    private var currentMotionProfile: MotionProfile? = null
    private var currentMotionState: MotionState? = null

    private var hasFinishedProfile: Boolean = false

    fun generateAndFollowMotionProfile(startPosition: Double, startV: Double, endPosition: Double, endV: Double) {
        val startState = MotionState(startPosition, startV)
        val endState = MotionState(endPosition, endV)

        currentMotionProfile = MotionProfileGenerator.generateSimpleMotionProfile(
            startState,
            endState,
            config.maxVelocity,
            config.maxAcceleration,
            0.0
        )

        hasFinishedProfile = false
        reset()
        motionTimer.reset()
    }

    override fun update(): Double {
        when {
            currentMotionProfile == null -> throw Exception("MUST BE FOLLOWING MOTION PROFILE")

            motionTimer.seconds() > currentMotionProfile!!.duration() -> {
                hasFinishedProfile = true
                currentMotionProfile = null
                currentMotionState = null
            }

            else -> {
                currentMotionState = currentMotionProfile!![motionTimer.seconds()]
                setControllerTargets(
                    currentMotionState!!.x,
                    currentMotionState!!.v,
                    currentMotionState!!.a
                )
            }
        }

        return super.update()
    }
}
