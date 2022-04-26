package com.asiankoala.koawalib.command.group

import com.asiankoala.koawalib.command.commands.Cmd

class RaceGroup(vararg cmds: Cmd) : ParallelGroup({ it.containsValue(false) }, *cmds)