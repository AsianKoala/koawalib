package com.asiankoala.koawalib.gamepad

import com.asiankoala.koawalib.control.filter.Debouncer
import com.asiankoala.koawalib.util.Periodic

// todo: find better way to do this
class KTrigger(private val triggerState: () -> Double) : KButton({ triggerState.invoke() > threshold }), Periodic {
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

    companion object {
        private val threshold = 0.3
    }
}
