package com.asiankoala.koawalib

import kotlin.test.Test

class Random {
    enum class bruh {
        a, b, c;
        fun next(): bruh {
            return values()[if(ordinal+1>values().size-1) 0 else ordinal+1]
        }
    }

    @Test
    fun m() {
        val t = bruh.c
        println(t.next())
        println(t.next().next())
        println(t.next().next().next())

    }
}