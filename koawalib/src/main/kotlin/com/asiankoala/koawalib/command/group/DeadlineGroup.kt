package com.asiankoala.koawalib.command.group

import com.asiankoala.koawalib.command.commands.Cmd

class DeadlineGroup(deadline: Cmd, vararg parallel: Cmd) : ParallelGroup({ deadline.isFinished }, deadline, *parallel)
