package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.command.group.CommandGroupBase

// choose between 2 commands based on the condition (true, false)
// CHOOSES ON INIT OF THIS COMMAND
class ConditionalCommand(onTrue: Command, onFalse: Command, condition: () -> Boolean) : CommandBase() {
    private val mOnTrue: Command
    private val mOnFalse: Command
    private val mCondition: () -> Boolean
    private lateinit var m_selectedCommand: Command

    override fun initialize() {
        m_selectedCommand = if (mCondition.invoke()) {
            mOnTrue
        } else {
            mOnFalse
        }

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

    /**
     * Creates a new ConditionalCommand.
     *
     * @param onTrue    the command to run if the condition is true
     * @param onFalse   the command to run if the condition is false
     * @param condition the condition to determine which command to run
     */
    init {
        CommandGroupBase.requireUngrouped(onTrue, onFalse)
        CommandGroupBase.registerGroupedCommands(onTrue, onFalse)
        mOnTrue = onTrue
        mOnFalse = onFalse
        mCondition = condition
        mRequirements.addAll(mOnTrue.getRequirements())
        mRequirements.addAll(mOnFalse.getRequirements())
    }
}
