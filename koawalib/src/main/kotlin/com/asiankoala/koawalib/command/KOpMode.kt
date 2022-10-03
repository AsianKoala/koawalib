package com.asiankoala.koawalib.command

import com.asiankoala.koawalib.command.commands.LoopCmd
import com.asiankoala.koawalib.gamepad.KGamepad
import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.hardware.motor.KMotor
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.logger.TimeInterval
import com.asiankoala.koawalib.logger.TimeIntervalManager
import com.asiankoala.koawalib.util.OpModeState
import com.asiankoala.koawalib.util.containsBy
import com.asiankoala.koawalib.util.internal.statemachine.StateMachine
import com.asiankoala.koawalib.util.internal.statemachine.StateMachineBuilder
import com.outoftheboxrobotics.photoncore.PhotonCore
import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.VoltageSensor
import com.qualcomm.robotcore.util.ElapsedTime

/**
 * The template opmode for utilizing koawalib. DO NOT OVERRIDE runOpMode(). Iterative OpMode's init, init loop, start, and loop functions have been
 * implemented with mInit(), mInitLoop(), mStart(), mLoop(), mStop()
 */
// @Suppress("unused")
abstract class KOpMode(
    private val photonEnabled: Boolean = false,
    private val maxParallelCommands: Int = 4
) : LinearOpMode() {
    var opmodeState = OpModeState.INIT
        private set

    protected val driver: KGamepad by lazy { KGamepad(gamepad1) }
    protected val gunner: KGamepad by lazy { KGamepad(gamepad2) }

    // let this be public for user opmodes
    private var opModeTimer = ElapsedTime()

    private var loopTimer = ElapsedTime()
    private lateinit var hubs: List<LynxModule>

    private lateinit var voltageSensor: VoltageSensor

    private fun setup() {
        KScheduler.stateReceiver = { opmodeState }

        Logger.reset()
        Logger.telemetry = telemetry
        KScheduler.resetScheduler()
        Logger.addWarningCountCommand()

        Logger.timeIntervalManager["HUB SETUP"].start()
        KDevice.hardwareMap = hardwareMap
        hubs = hardwareMap.getAll(LynxModule::class.java)
        hubs.forEach { it.bulkCachingMode = LynxModule.BulkCachingMode.MANUAL }
        voltageSensor = hardwareMap.voltageSensor.iterator().next()
        Logger.timeIntervalManager["HUB SETUP"].end()

        Logger.timeIntervalManager["PHOTON SETUP"].start()
        if (photonEnabled) {
            PhotonCore.enable()
            PhotonCore.experimental.setMaximumParallelCommands(maxParallelCommands)
        }
        Logger.timeIntervalManager["PHOTON END"].end()

        opModeTimer.reset()
        Logger.timeIntervalManager.log()
        Logger.logInfo("OpMode set up")
    }

    private fun schedulePeriodics() {
        + LoopCmd(driver::periodic).withName("driver gamepad periodic")
        + LoopCmd(gunner::periodic).withName("gunner gamepad periodic")
        + LoopCmd({ hubs.forEach(LynxModule::clearBulkCache) }).withName("clear bulk data periodic")
        + LoopCmd(::handleLoopMsTelemetry).withName("loop ms telemetry periodic")
        Logger.logInfo("periodics scheduled")
    }

    private fun handleLoopMsTelemetry() {
        val dt = loopTimer.milliseconds()
        loopTimer.reset()
        telemetry.addData("hz", 1000.0 / dt)
        telemetry.addData("loop time", dt)
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
        if (Logger.config.isTelemetryEnabled) {
            telemetry.update()
        }
    }

    private fun checkIfTelemetryNeeded() {
        if (!Logger.config.isTelemetryEnabled) {
            telemetry.msTransmissionInterval = 100000
            Logger.logInfo("Telemetry disabled")
        } else {
            telemetry.msTransmissionInterval = 250
        }
    }

    private val mainStateMachine: StateMachine<OpModeState> = StateMachineBuilder<OpModeState>()
        .universal(KScheduler::update)
        .universal(Logger::update)
        .universal(::updateTelemetryIfEnabled)
        .state(OpModeState.INIT)
        .onEnter { Logger.timeIntervalManager["INIT"].start() }
        .onEnter(::setup)
        .onEnter(::schedulePeriodics)
        .onEnter(::mInit)
        .onEnter { Logger.timeIntervalManager["INIT"].end() }
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
        .transition { true }
        .build()

    override fun runOpMode() {
        mainStateMachine.start()

        while (mainStateMachine.running) {
            mainStateMachine.update()
            opmodeState = mainStateMachine.state
        }
    }

    abstract fun mInit()
    open fun mInitLoop() {}
    open fun mStart() {}
    open fun mLoop() {}
    open fun mStop() {}
}
