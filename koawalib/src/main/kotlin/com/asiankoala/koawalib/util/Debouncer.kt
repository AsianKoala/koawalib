package com.asiankoala.koawalib.util

import com.acmerobotics.roadrunner.util.NanoClock

/**
 * @see https://github.com/wpilibsuite/allwpilib/blob/main/wpimath/src/main/java/edu/wpi/first/math/filter/Debouncer.java
 * A simple debounce filter for boolean streams. Requires that the boolean change value from
 * baseline for a specified period of time before the filtered value changes.
 *
 * note: ported from wpilib
 */
@Suppress("unused")
class Debouncer @JvmOverloads constructor(
    private val m_debounceTimeSeconds: Double,
    private val m_debounceType: DebounceType = DebounceType.kBoth
) {
    enum class DebounceType {
        kRising, kFalling, kBoth
    }

    private var m_baseline = false
    private var m_prevTimeSeconds = 0.0
    private fun resetTimer() {
        m_prevTimeSeconds = NanoClock.system().seconds()
    }

    private fun hasElapsed(): Boolean {
        return NanoClock.system().seconds() - m_prevTimeSeconds >= m_debounceTimeSeconds
    }

    /**
     * Applies the debouncer to the input stream.
     *
     * @param input The current value of the input stream.
     * @return The debounced value of the input stream.
     */
    fun calculate(input: Boolean): Boolean {
        if (input == m_baseline) {
            resetTimer()
        }
        return if (hasElapsed()) {
            if (m_debounceType == DebounceType.kBoth) {
                m_baseline = input
                resetTimer()
            }
            input
        } else {
            m_baseline
        }
    }
    /**
     * Creates a new Debouncer.
     *
     * @param m_debounceTimeSeconds The number of seconds the value must change from baseline for the filtered
     * value to change.
     * @param m_debounceType Which type of state change the debouncing will be performed on.
     */
    /**
     * Creates a new Debouncer. Baseline value defaulted to "false."
     *
     * @param debounceTime The number of seconds the value must change from baseline for the filtered
     * value to change.
     */
    init {
        resetTimer()
        m_baseline = when (m_debounceType) {
            DebounceType.kBoth, DebounceType.kRising -> false
            DebounceType.kFalling -> true
        }
    }
}
