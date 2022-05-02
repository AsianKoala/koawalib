package com.asiankoala.koawalib.command.group

import com.asiankoala.koawalib.command.commands.Command

interface Group {
    fun addCommands(vararg commands: Command)
}
