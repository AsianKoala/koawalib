package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.subsystem.old.MotorSubsystem

class MotionProfileCommand(private val target: Double, private val subsystem: MotorSubsystem) : CommandBase() {

    override fun initialize() {
        subsystem.generateAndFollowMotionProfile(target)
    }

    override fun execute() {

    }

    override val isFinished: Boolean
        get() = subsystem.isAtTarget

    init {
        addRequirements(subsystem)
    }

}