package com.asiankoala.koawalib.command.group

import com.asiankoala.koawalib.command.commands.Command

class RaceGroup(vararg commands: Command) : ParallelGroup({ it.containsValue(false) }, *commands)
