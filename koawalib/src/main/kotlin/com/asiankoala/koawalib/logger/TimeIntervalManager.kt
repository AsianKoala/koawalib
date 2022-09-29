package com.asiankoala.koawalib.logger

class TimeIntervalManager {
    private val timeIntervals = LinkedHashMap<String, TimeInterval>()
    operator fun get(name: String): TimeInterval {
        return if(name in timeIntervals.keys) {
            timeIntervals[name]!!
        } else {
            timeIntervals[name] = TimeInterval()
            timeIntervals[name]!!
        }
    }
}