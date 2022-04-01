package com.asiankoala.koawalib.subsystem.old

import com.asiankoala.koawalib.command.commands.CommandBase

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