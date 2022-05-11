package com.asiankoala.koawalib.util

import kotlin.test.Test

class RateLimitTest {
    @Test
    fun testRateLimit() {
        val l = ArrayList<Int>()
        val r = RateLimiter(0.1) { l.add(0) }
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < 1000) {
            r.periodic()
        }
        assert(l.size in 9..11)
    }
}
