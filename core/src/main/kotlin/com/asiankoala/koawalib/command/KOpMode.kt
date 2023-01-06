package com.asiankoala.koawalib.command

import com.asiankoala.koawalib.command.commands.LoopCmd
import com.asiankoala.koawalib.gamepad.KGamepad
import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.hardware.motor.KMotor
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.util.OpModeState
import com.asiankoala.koawalib.util.internal.statemachine.StateMachine
import com.asiankoala.koawalib.util.internal.statemachine.StateMachineBuilder
import com.outoftheboxrobotics.photoncore.PhotonCore
import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.VoltageSensor
import com.qualcomm.robotcore.util.ElapsedTime

/**
 * The template opmode for utilizing koawalib
 */
abstract class KOpMode(
    private val photonEnabled: Boolean = false,
) : LinearOpMode() {
    var opModeState = OpModeState.INIT
        private set

    protected val driver: KGamepad by lazy { KGamepad(gamepad1) }
    protected val gunner: KGamepad by lazy { KGamepad(gamepad2) }

    private var opModeTimer = ElapsedTime()
    private var loopTimer = ElapsedTime()
    private lateinit var hubs: List<LynxModule>
    private lateinit var voltageSensor: VoltageSensor

    private fun setupLib() {
        KScheduler.stateReceiver = { opModeState }
        Logger.reset()
        Logger.telemetry = telemetry
        KScheduler.resetScheduler()
        Logger.addWarningCountCommand()
    }

    private fun setupHardware() {
        KDevice.hardwareMap = hardwareMap
        if (photonEnabled) {
            PhotonCore.experimental.setMaximumParallelCommands(8)
            PhotonCore.CONTROL_HUB.bulkCachingMode = LynxModule.BulkCachingMode.MANUAL
            PhotonCore.EXPANSION_HUB.bulkCachingMode = LynxModule.BulkCachingMode.MANUAL
            PhotonCore.enable()
        } else {
            hubs = hardwareMap.getAll(LynxModule::class.java)
            hubs.forEach { it.bulkCachingMode = LynxModule.BulkCachingMode.MANUAL }
        }
        voltageSensor = hardwareMap.voltageSensor.iterator().next()
        KMotor.lastVoltageRead = voltageSensor.voltage
    }

    private fun setup() {
        setupLib()
        setupHardware()
        opModeTimer.reset()
        Logger.logInfo("OpMode set up")
    }

    private fun schedulePeriodics() {
        + LoopCmd(driver::periodic).withName("driver gamepad periodic")
        + LoopCmd(gunner::periodic).withName("gunner gamepad periodic")
        + LoopCmd(::handleBulkCaching).withName("clear bulk data periodic")
        + LoopCmd(::handleLoopMsTelemetry).withName("loop ms telemetry periodic")
        + LoopCmd(KMotor::updatePriorityIter).withName("motor priority periodic")
        Logger.logInfo("periodics scheduled")
    }

    private fun handleBulkCaching() {
        if (photonEnabled) {
            PhotonCore.CONTROL_HUB.clearBulkCache()
            PhotonCore.EXPANSION_HUB.clearBulkCache()
        } else {
            hubs.forEach(LynxModule::clearBulkCache)
        }
    }

    private fun handleLoopMsTelemetry() {
        val dt = loopTimer.milliseconds()
        loopTimer.reset()
        telemetry.addData("hz", 1000.0 / dt)
        telemetry.addData("loop time", dt)
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
            telemetry.msTransmissionInterval = 500
        }
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
        .loop(::mInitLoop)
        .transition(::isStarted)
        .state(OpModeState.START)
        .onEnter(::mStart)
        .onEnter(opModeTimer::reset)
        .onEnter { Logger.logInfo("OpMode started") }
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
            opModeState = mainStateMachine.state
        }
    }

    abstract fun mInit()
    open fun mInitLoop() {}
    open fun mStart() {}
    open fun mLoop() {}
    open fun mStop() {}
}
