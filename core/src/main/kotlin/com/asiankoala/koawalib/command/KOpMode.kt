package com.asiankoala.koawalib.command

import com.asiankoala.koawalib.command.commands.LoopCmd
import com.asiankoala.koawalib.gamepad.KGamepad
import com.asiankoala.koawalib.hardware.KDevice
import com.asiankoala.koawalib.hardware.motor.KMotor
import com.asiankoala.koawalib.logger.Logger
import com.asiankoala.koawalib.util.OpModeState
import com.outoftheboxrobotics.photoncore.PhotonCore
import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.VoltageSensor
import com.qualcomm.robotcore.util.ElapsedTime

/**
 * The base opmode for utilizing koawalib
 */
abstract class KOpMode @JvmOverloads constructor(
    private val photonEnabled: Boolean = false,
    private val maxParallelCommands: Int = 6
) : LinearOpMode() {
    private var opModeTimer = ElapsedTime()
    private var loopTimer = ElapsedTime()
    private lateinit var hubs: List<LynxModule>
    private lateinit var voltageSensor: VoltageSensor
    var opModeState = OpModeState.INIT
        private set
    protected val driver: KGamepad by lazy { KGamepad(gamepad1) }
    protected val gunner: KGamepad by lazy { KGamepad(gamepad2) }

    private fun setupLib() {
        KScheduler.stateReceiver = { opModeState }
        Logger.reset()
        Logger.telemetry = telemetry
        KScheduler.resetScheduler()
        Logger.addWarningCountCommand()
        if (!Logger.config.isTelemetryEnabled) Logger.logInfo("Telemetry disabled")
    }

    private fun setupHardware() {
        KDevice.hardwareMap = hardwareMap
        if (photonEnabled) {
            PhotonCore.experimental.setMaximumParallelCommands(maxParallelCommands)
            PhotonCore.CONTROL_HUB?.bulkCachingMode = LynxModule.BulkCachingMode.MANUAL
            PhotonCore.EXPANSION_HUB?.bulkCachingMode = LynxModule.BulkCachingMode.MANUAL
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
            PhotonCore.CONTROL_HUB?.clearBulkCache()
            PhotonCore.EXPANSION_HUB?.clearBulkCache()
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

    final override fun runOpMode() {
        isStarted // holy fucking jank LMFAO
        loop@ while(opModeIsActive()) {
            KScheduler.update()
            Logger.update()
            if (Logger.config.isTelemetryEnabled) telemetry.update()

            when(opModeState) {
                OpModeState.INIT -> {
                    setup()
                    schedulePeriodics()
                    mInit()
                    Logger.logInfo("Fully initialized")
                    opModeState = OpModeState.INIT_LOOP
                }

                OpModeState.INIT_LOOP -> {
                    mInitLoop()
                    if(isStarted) opModeState = OpModeState.START
                }

                OpModeState.START -> {
                    mStart()
                    opModeTimer.reset()
                    Logger.logInfo("OpMode started!")
                    opModeState = OpModeState.LOOP
                }

                OpModeState.LOOP -> {
                    mLoop()
                }

                OpModeState.STOP -> {
                    mStop()
                    break@loop
                }
            }
        }
    }

    abstract fun mInit()
    open fun mInitLoop() {}
    open fun mStart() {}
    open fun mLoop() {}
    open fun mStop() {}
}
