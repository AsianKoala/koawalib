package com.asiankoala.koawalib.command.group

import com.asiankoala.koawalib.command.commands.Command

class DeadlineGroup(deadline: Command, vararg parallel: Command) : ParallelGroup({ deadline.isFinished }, deadline, *parallel)