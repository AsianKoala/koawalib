package com.asiankoala.koawalib

import com.asiankoala.koawalib.util.containsBy
import kotlin.test.Test

class ExtensionsTest {
    @Test
    fun testContainsBy() {
        val l = listOf(
            Pair(1, 2),
            Pair(3, 4),
            Pair(5, 6)
        )

        val result = l.containsBy({ it.second }, 6)
        val expected = l.map { it.second }.contains(6)
        assert(result == expected)
    }
}