package com.asiankoala.koawalib.command.group

import com.asiankoala.koawalib.command.commands.Cmd

open class ForCmd(private val cmd: Cmd, private val num: Int) : Cmd() {
    private var idx = 0

    override fun initialize() {
        cmd.initialize()
    }

    override fun execute() {
        cmd.execute()
        if(cmd.isFinished) {
            cmd.end()
            idx++
        }
    }

    override val isFinished: Boolean
        get() = idx == num
}