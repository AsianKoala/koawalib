package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.subsystem.Subsystem

/**
 * Command that runs infinitely
 * @param action action to run
 * @param requirements subsystem requirements
 */
class LoopCmd(
    private val action: () -> Unit,
    vararg requirements: Subsystem
) : LoopUntilCmd(action, { false }, *requirements)
