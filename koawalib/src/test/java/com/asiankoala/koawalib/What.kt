package com.asiankoala.koawalib

import com.asiankoala.koawalib.command.CommandScheduler
import com.asiankoala.koawalib.command.commands.PrintCommand

object What {
    class TestCommand : PrintCommand("wtf")
    class OtherTestCommand : PrintCommand("bruh")
    @JvmStatic
    fun main(args: Array<String>) {
        CommandScheduler.resetScheduler()
        CommandScheduler.stopLogging()
        val a = TestCommand()
        val b = TestCommand()
        val c = TestCommand()
        a.schedule()
        b.andThen(c).schedule()
        b.schedule()
        CommandScheduler.run()
        CommandScheduler.run()
    }
}
