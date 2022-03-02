package com.asiankoala.koawalib.dashboard

import com.acmerobotics.dashboard.FtcDashboard
import com.acmerobotics.dashboard.canvas.Canvas
import org.firstinspires.ftc.robotcore.external.Telemetry

object KoawaDashboard {
    private var dashboardAdapter = TelemetryAdapter()
    private lateinit var telemetryImpl: Telemetry
    private var isUpdatingDashboard: Boolean = false

    fun init(telemImpl: Telemetry, shouldUpdate: Boolean) {
        telemetryImpl = telemImpl
        isUpdatingDashboard = shouldUpdate
    }

    fun fieldOverlay(): Canvas? {
        return if (isUpdatingDashboard) {
            dashboardAdapter.fieldOverlay()
        } else {
            null
        }
    }

    fun setHeader(v: String) {
        addSpace()
        addLine("------$v------")
    }

    fun addLine(v: String) {
        if (isUpdatingDashboard) {
            dashboardAdapter.addLine(v)
        }

        telemetryImpl.addLine(v)
    }

    fun addLine() {
        addLine("")
    }

    fun addSpace() {
        addLine(" ")
    }

    fun update() {
        if (isUpdatingDashboard) {
            FtcDashboard.getInstance().sendTelemetryPacket(dashboardAdapter)
            dashboardAdapter = TelemetryAdapter()
        }
        telemetryImpl.update()
        telemetryImpl.clearAll()
    }

    operator fun set(k: String, v: Any) {
        telemetryImpl.addData(k, v)

        if (isUpdatingDashboard) {
            dashboardAdapter.put(k, v)
        }
    }
}
