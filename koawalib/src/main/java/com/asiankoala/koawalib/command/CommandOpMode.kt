package com.asiankoala.koawalib.command

import com.asiankoala.koawalib.gamepad.KGamepad
import com.asiankoala.koawalib.statemachine.StateMachineBuilder
import com.asiankoala.koawalib.util.OpModeState
import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.util.ElapsedTime

@Suppress("unused")
open class CommandOpMode : LinearOpMode() {
    protected lateinit var driver: KGamepad
    protected lateinit var gunner: KGamepad

    private var prevLoopTime = System.currentTimeMillis()
    private var opModeTimer = ElapsedTime()
    private lateinit var hubs: List<LynxModule>

    val isLooping get() = mainStateMachine.state == OpModeState.LOOP

    private fun setup() {
        CommandScheduler.resetScheduler()

        hubs = hardwareMap.getAll(LynxModule::class.java)
        hubs.forEach { it.bulkCachingMode = LynxModule.BulkCachingMode.MANUAL }

        driver = KGamepad(gamepad1)
        gunner = KGamepad(gamepad2)
        opModeTimer.reset()
    }

    private fun schedulePeriodics() {
        CommandScheduler.addPeriodic { prevLoopTime = System.currentTimeMillis() }
        CommandScheduler.addPeriodic(driver::periodic)
        CommandScheduler.addPeriodic(gunner::periodic)
        CommandScheduler.addPeriodic { hubs.forEach(LynxModule::clearBulkCache) }
    }

    private fun handleLoopMsTelemetry() {
        val dt = System.currentTimeMillis() - prevLoopTime
        telemetry.addData("loop ms", dt)
    }

    private val mainStateMachine = StateMachineBuilder<OpModeState>()
        .universal(CommandScheduler::run)
        .universal(::mUniversal)
        .state(OpModeState.INIT)
        .onEnter(::setup)
        .onEnter(::schedulePeriodics)
        .onEnter(::mInit)
        .transition { true }
        .state(OpModeState.INIT_LOOP)
        .loop(::mInitLoop)
        .transition(::isStarted)
        .state(OpModeState.START)
        .onEnter(::mStart)
        .onEnter(opModeTimer::reset)
        .onEnter(CommandScheduler::startOpModeLooping)
        .transition { true }
        .state(OpModeState.LOOP)
        .loop(::mLoop)
        .loop(::handleLoopMsTelemetry)
        .transition(::isStopRequested)
        .state(OpModeState.STOP)
        .onEnter(::mStop)
        .onEnter(CommandScheduler::resetScheduler)
        .onEnter(opModeTimer::reset)
        .transition { true }
        .build()

    override fun runOpMode() {
        mainStateMachine.reset()
        mainStateMachine.start()

        while (mainStateMachine.running) {
            mainStateMachine.update()
        }
    }

    open fun mInit() {}
    open fun mInitLoop() {}
    open fun mStart() {}
    open fun mLoop() {}
    open fun mStop() {}
    open fun mUniversal() {}
}
