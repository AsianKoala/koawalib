package com.asiankoala.koawalib.gamepad

import com.asiankoala.koawalib.gamepad.functionality.Button
import com.asiankoala.koawalib.util.Periodic
import com.asiankoala.koawalib.util.Debouncer

class KTrigger(private val triggerState: () -> Double) : Button(), Periodic {
    private var threshold = 0.3
    private var debounceSeconds = 0.25
    private var debouncer = Debouncer(debounceSeconds)
    private var debouncerEnabled = false
    private var debouncerState = false
    private val isTriggerPressed get() = triggerState.invoke() > threshold

    override fun periodic() {
        if (debouncerEnabled) {
            debouncerState = debouncer.calculate(isTriggerPressed)
        }
    }

    override fun invokeBoolean(): Boolean {
        return if (debouncerEnabled) debouncerState else isTriggerPressed
    }
}
