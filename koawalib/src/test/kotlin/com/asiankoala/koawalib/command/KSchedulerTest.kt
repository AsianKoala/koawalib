package com.asiankoala.koawalib.command

import com.asiankoala.koawalib.command.commands.Cmd
import com.asiankoala.koawalib.command.commands.InstantCmd
import com.asiankoala.koawalib.command.commands.WaitCmd
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.subsystem.DeviceSubsystem
import com.asiankoala.koawalib.testPeriodic
import com.asiankoala.koawalib.testReset
import kotlin.test.Test

internal class KSchedulerTest {
    @Test
    fun testScheduler() {
        testReset()
        + InstantCmd({ Logger.logInfo("meow") })
        testPeriodic()
        + InstantCmd({ Logger.logInfo("hi") })
        testPeriodic()
    }

    @Test
    fun testDefaultCommands() {
        testReset()
        class neko : DeviceSubsystem()
        class nekoCmd(n: neko) : Cmd() {
            override fun execute() {
                Logger.logInfo("nyaa")
            }

            init {
                addRequirements(n)
            }
        }
        val n = neko()
        val cmd = nekoCmd(n)
        n.setDefaultCommand(cmd)
        testPeriodic()
    }

    @Test
    fun testCanceling() {
        testReset()
        class nyaCmd : WaitCmd(5.0) {
            override fun execute() {
                Logger.logInfo("nyaa")
            }
        }
        val nya = nyaCmd()
        + nya
        testPeriodic()
        - nya
        testPeriodic()
    }

    @Test
    fun testDeviceRegistry() {
//        testReset()
//        TestDevice("nya")
//        assertEquals(1, KScheduler.deviceRegistry.size)
    }
}
