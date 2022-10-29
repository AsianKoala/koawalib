package com.asiankoala.koawalib.gamepad

import com.asiankoala.koawalib.control.filter.Debouncer
import com.asiankoala.koawalib.gamepad.functionality.ButtonProcessing

// todo: find better way to do this
class KTrigger(
    private val triggerState: () -> Double,
) : ButtonProcessing() {
    var threshold = 0.3
    var debounceSeconds = 0.25
    var debouncerEnabled = false

    private val isTriggerPressed get() = triggerState.invoke() > threshold
    private var debouncer = Debouncer(debounceSeconds)
    private var debouncerState = false

    override fun periodic() {
        super.periodic()
        if (debouncerEnabled) {
            debouncerState = debouncer.calculate(isTriggerPressed)
        }
    }

    override fun invokeBoolean(): Boolean {
        return if (debouncerEnabled) debouncerState else isTriggerPressed
    }
}
