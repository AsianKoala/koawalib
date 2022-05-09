package com.asiankoala.koawalib.util

import com.qualcomm.robotcore.util.ElapsedTime

class RateLimit(var rateLimitMs: Double) {
    private var timer = ElapsedTime()

    fun isSafeToUpdate(): Boolean {
        if(timer.milliseconds() > rateLimitMs) {
            timer.reset()
            return true
        }
        return false
    }
}