package com.asiankoala.koawalib.command.group

import com.asiankoala.koawalib.command.commands.Cmd

interface Group {
    fun addCommands(vararg cmds: Cmd)
}
