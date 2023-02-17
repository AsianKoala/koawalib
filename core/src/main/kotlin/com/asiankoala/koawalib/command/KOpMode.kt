package com.asiankoala.koawalib.command

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
    private val photonEnabled: Boolean = true,
    private val maxParallelCommands: Int = 6
) : LinearOpMode() {
    var opModeState = OpModeState.INIT; private set
    private enum class InternalState { INIT, LOOP, STOP }
    private val internalState get() = when {
        isStopRequested -> InternalState.STOP
        isStarted -> InternalState.LOOP
        else -> InternalState.INIT
    }
    private var hasStarted = false
    private var loopTimer = ElapsedTime()
    private lateinit var hubs: List<LynxModule>
    private lateinit var voltageSensor: VoltageSensor
    protected val driver: KGamepad by lazy { KGamepad(gamepad1) }
    protected val gunner: KGamepad by lazy { KGamepad(gamepad2) }

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
        Logger.logInfo("OpMode set up")
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

    private val universalActions = listOf(
        driver::periodic,
        gunner::periodic,
        ::handleBulkCaching,
        ::handleLoopMsTelemetry,
        KMotor::updatePriorityIter,
        KScheduler::update,
        Logger::update,
    )

    private val initActions = listOf(
        ::setup,
        ::mInit,
        { Logger.logInfo("Fully initialized") }
    )

    private val startActions = listOf(
        ::mStart,
        { Logger.logInfo("OpMode started") },
        { hasStarted = true }
    )

    final override fun runOpMode() {
        initActions.forEach { it.invoke() }
        eventLoop@ while(true) {
            universalActions.forEach { it.invoke() }
            when(internalState) {
                InternalState.INIT -> mInitLoop()
                InternalState.LOOP -> {
                    if(hasStarted) mLoop() else startActions.forEach { it.invoke() }
                }
                InternalState.STOP -> break@eventLoop
            }
        }
        mStop()
    }

    abstract fun mInit()
    open fun mInitLoop() {}
    open fun mStart() {}
    open fun mLoop() {}
    open fun mStop() {}
}
