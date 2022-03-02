package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.subsystem.Subsystem

class InlineCommand(
    private val mOnInit: () -> Unit = {},
    private val mOnExecute: () -> Unit = {},
    private val mOnFinish: (Boolean) -> Unit = {},
    private val mIsFinished: () -> Boolean = { false },
    vararg requirements: Subsystem
) : CommandBase() {

    override fun init() {
        mOnInit.invoke()
    }

    override fun execute() {
        mOnExecute.invoke()
    }

    override fun end(interrupted: Boolean) {
        mOnFinish.invoke(interrupted)
    }

    override val isFinished: Boolean get() = mIsFinished.invoke()

    init {
        addRequirements(*requirements)
    }
}
