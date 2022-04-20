package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.command.group.CommandGroupBase

/**
 * Command that chooses between two different commands based on condition
 * @param onTrue    the command to run if the condition is true
 * @param onFalse   the command to run if the condition is false
 * @param condition the condition to determine which command to run
 */
@Suppress("unused")
class ConditionalCommand(private val onTrue: Command, private val onFalse: Command, private val condition: () -> Boolean) : CommandBase() {
    private val m_selectedCommand by lazy { if(condition.invoke()) onTrue else onFalse }

    override fun initialize() {
        m_selectedCommand.initialize()
    }

    override fun execute() {
        m_selectedCommand.execute()
    }

    override fun end() {
        m_selectedCommand.end()
    }

    override val isFinished: Boolean
        get() = m_selectedCommand.isFinished

    init {
        CommandGroupBase.assertUngrouped(onTrue, onFalse)
        CommandGroupBase.registerGroupedCommands(onTrue, onFalse)
        mRequirements.addAll(onTrue.getRequirements())
        mRequirements.addAll(onFalse.getRequirements())
    }
}
