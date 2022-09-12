package com.asiankoala.koawalib.command

import com.asiankoala.koawalib.command.commands.LoopCmd
import com.asiankoala.koawalib.gamepad.KGamepad
import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.hardware.motor.KMotor
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.statemachine.StateMachine
import com.asiankoala.koawalib.statemachine.StateMachineBuilder
import com.asiankoala.koawalib.util.OpModeState
import com.asiankoala.koawalib.util.containsBy
import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.VoltageSensor
import com.qualcomm.robotcore.util.ElapsedTime

/**
 * The template opmode for utilizing koawalib. DO NOT OVERRIDE runOpMode(). Iterative OpMode's init, init loop, start, and loop functions have been
 * implemented with mInit(), mInitLoop(), mStart(), mLoop(), mStop()
 */
// @Suppress("unused")
abstract class KOpMode : LinearOpMode() {
    protected val driver: KGamepad by lazy { KGamepad(gamepad1) }
    protected val gunner: KGamepad by lazy { KGamepad(gamepad2) }

    // let this be public for user opmodes
    var opmodeState = OpModeState.INIT
        private set

    private var prevLoopTime = System.currentTimeMillis()
    private var opModeTimer = ElapsedTime()
    private lateinit var hubs: List<LynxModule>

    private lateinit var voltageSensor: VoltageSensor

    private fun secondaryLoop() {
        opmodeState = mainStateMachine.state
        prevLoopTime = System.currentTimeMillis()
    }

    private fun setup() {
        KScheduler.stateReceiver = { opmodeState }

        Logger.reset()
        Logger.telemetry = telemetry
        KScheduler.resetScheduler()
        Logger.addWarningCountCommand()

        KDevice.hardwareMap = hardwareMap
        hubs = hardwareMap.getAll(LynxModule::class.java)
        hubs.forEach { it.bulkCachingMode = LynxModule.BulkCachingMode.MANUAL }
        voltageSensor = hardwareMap.voltageSensor.iterator().next()

        opModeTimer.reset()
        Logger.logInfo("opmode set up")
    }

    private fun schedulePeriodics() {
        + LoopCmd(driver::periodic).withName("driver gamepad periodic")
        + LoopCmd(gunner::periodic).withName("gunner gamepad periodic")
        + LoopCmd({ hubs.forEach(LynxModule::clearBulkCache) }).withName("clear bulk data periodic")
        + LoopCmd(::handleLoopMsTelemetry).withName("loop ms telemetry periodic")
        Logger.logInfo("periodics scheduled")
    }

    private fun handleLoopMsTelemetry() {
        val dt = System.currentTimeMillis() - prevLoopTime
        telemetry.addData("hz", 1000.0 / dt)
    }

    private fun checkIfVoltageSensorNeeded() {
        if (KScheduler.deviceRegistry.values
            .filterIsInstance<KMotor>()
            .containsBy({ it.isVoltageCorrected }, true)
        ) {
            + LoopCmd({ KDevice.lastVoltageRead = voltageSensor.voltage }).withName("voltage sensor periodic")
            Logger.logInfo("Voltage read scheduled")
        } else {
            Logger.logInfo("Voltage sensor not enabled")
        }
    }

    private fun updateTelemetryIfEnabled() {
        if(Logger.config.isTelemetryEnabled) {
            telemetry.update()
        }
    }

    // TODO: have a "competition mode" where this is run automatically
    private fun checkIfTelemetryNeeded() {
        if(!Logger.config.isTelemetryEnabled) {
            telemetry.msTransmissionInterval = 100000
        }
        Logger.logInfo("Telemetry not enabled")
    }

    private val mainStateMachine: StateMachine<OpModeState> = StateMachineBuilder<OpModeState>()
        .universal(KScheduler::update)
        .universal(Logger::update)
        .universal(::updateTelemetryIfEnabled)
        .state(OpModeState.INIT)
        .onEnter(::setup)
        .onEnter(::schedulePeriodics)
        .onEnter(::mInit)
        .onEnter { Logger.logInfo("fully initialized, entering init loop") }
        .transition { true }
        .state(OpModeState.INIT_LOOP)
        .onEnter(::checkIfTelemetryNeeded)
        .onEnter(::checkIfVoltageSensorNeeded)
        .loop(::mInitLoop)
        .transition(::isStarted)
        .state(OpModeState.START)
        .onEnter(::mStart)
        .onEnter(opModeTimer::reset)
        .onEnter { Logger.logInfo("opmode started") }
        .transition { true }
        .state(OpModeState.LOOP)
        .loop(::mLoop)
        .transition(::isStopRequested)
        .state(OpModeState.STOP)
        .onEnter(::mStop)
        .onEnter(opModeTimer::reset)
        .transition { true } //
        .build()

    override fun runOpMode() {
        mainStateMachine.start()

        while (mainStateMachine.running) {
            mainStateMachine.update()
            secondaryLoop()
        }
    }

    abstract fun mInit()
    open fun mInitLoop() {}
    open fun mStart() {}
    open fun mLoop() {}
    open fun mStop() {}
}
