package com.asiankoala.koawalib.util

enum class Alliance {
    BLUE, RED;

    fun <T> decide(blue: T, red: T): T {
        return if (this == BLUE) {
            blue
        } else {
            red
        }
    }
}
