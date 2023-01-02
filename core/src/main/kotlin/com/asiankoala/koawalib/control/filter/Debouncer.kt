package com.asiankoala.koawalib.control.filter

import com.qualcomm.robotcore.util.ElapsedTime

/**
 * A simple debounce filter for boolean streams. Requires that the boolean change value from
 * baseline for a specified period of time before the filtered value changes.
 */
class Debouncer(
    private val dt: Double,
) {
    private val timer = ElapsedTime()
    private var value = false

    /**
     * Applies the debouncer to the input stream.
     *
     * @param input The current value of the input stream.
     * @return The debounced value of the input stream.
     */
    fun calculate(input: Boolean): Boolean {
        if (input == value) timer.reset()
        if (timer.seconds() > dt) value = input
        return value
    }
}
