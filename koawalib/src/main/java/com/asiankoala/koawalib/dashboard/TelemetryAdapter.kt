package com.asiankoala.koawalib.dashboard

import com.acmerobotics.dashboard.telemetry.TelemetryPacket

class TelemetryAdapter : TelemetryPacket() {
    fun addData(k: String, v: Any) {
        addLine("$k: $v")
    }
}
