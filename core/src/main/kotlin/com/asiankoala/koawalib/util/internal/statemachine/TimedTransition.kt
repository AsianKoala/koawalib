package com.asiankoala.koawalib.util.internal.statemachine

import com.qualcomm.robotcore.util.ElapsedTime

internal class TimedTransition(private val ms: Double) : () -> Boolean {
    private val timer = ElapsedTime()

    fun resetTimer() {
        timer.reset()
    }

    override fun invoke(): Boolean {
        return timer.seconds() > ms
    }
}
