package com.asiankoala.koawalib.logger

class TimeIntervalManager {
    private val timeIntervals = LinkedHashMap<String, TimeInterval>()

    operator fun get(name: String): TimeInterval {
        return if(name in timeIntervals.keys) {
            timeIntervals[name]!!
        } else {
            timeIntervals[name] = TimeInterval()
            Logger.logInfo("created new time interval: $name")
            timeIntervals[name]!!
        }
    }

    fun log() {
        timeIntervals.forEach { (s, timeInterval) ->
            Logger.logInfo("TI $s: ${timeInterval.avgMs}")
        }
    }
}