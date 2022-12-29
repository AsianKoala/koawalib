package com.asiankoala.koawalib.control.filter

import com.qualcomm.robotcore.util.ElapsedTime

class Debouncer (
    private val dt: Double,
) {
    private val timer = ElapsedTime()
    private var value = false

    fun calculate(input: Boolean): Boolean {
        if(input == value) timer.reset()
        if(timer.seconds() > dt) value = input
        return value
    }
}
