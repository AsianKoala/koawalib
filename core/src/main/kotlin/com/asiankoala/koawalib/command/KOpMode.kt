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
    val opModeState get() = when {
        isStopRequested -> OpModeState.STOP
        isStarted -> OpModeState.PLAY
        else -> OpModeState.INIT
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
        Logger.put("hz", 1000.0 / dt)
        Logger.put("loop time", dt)
    }

    private val universalActions by lazy {
        listOf(
            driver::periodic,
            gunner::periodic,
            ::handleBulkCaching,
            ::handleLoopMsTelemetry,
            KMotor::updatePriorityIter,
            KScheduler::update,
            Logger::update,
            telemetry::update
        )
    }

    private val initActions by lazy {
        listOf(
            ::setup,
            ::mInit,
            { Logger.logInfo("Fully initialized") }
        )
    }

    private val startActions by lazy {
        listOf(
            ::mStart,
            { Logger.logInfo("OpMode started") },
            { hasStarted = true }
        )
    }

    final override fun runOpMode() {
        initActions.forEach { it.invoke() }
        eventLoop@ while (true) {
            universalActions.forEach { it.invoke() }
            when (opModeState) {
                OpModeState.INIT -> mInitLoop()
                OpModeState.PLAY -> {
                    if (hasStarted) mLoop() else startActions.forEach { it.invoke() }
                }
                OpModeState.STOP -> break@eventLoop
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
