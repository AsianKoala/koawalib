package com.asiankoala.koawalib.gamepad

import com.asiankoala.koawalib.control.filter.Debouncer
import com.asiankoala.koawalib.gamepad.functionality.ButtonProcessing

class KTrigger(
    private val triggerState: () -> Double,
) : ButtonProcessing() {
    private var threshold = 0.3
    private var debouncer: Debouncer? = null
    private val isTriggerPressed get() = triggerState.invoke() > threshold
    private var debouncerState = false

    override fun periodic() {
        super.periodic()
        debouncer?.let { debouncerState = it.calculate(isTriggerPressed) }
    }

    override fun invoke(): Boolean {
        return debouncer?.let { debouncerState } ?: isTriggerPressed
    }

    fun setDebouncer(d: Debouncer) {
        debouncer = d
    }

    fun setThreshold(t: Double) {
        threshold = t
    }
}
