package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.subsystem.KSubsystem

/**
 * Command that runs infinitely
 * @param action action to run
 * @param requirements subsystem requirements
 *
 */
class LoopCmd(
    private val action: () -> Unit,
    vararg requirements: KSubsystem
) : LoopUntilCmd(action, { false }, *requirements)
