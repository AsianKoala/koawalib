package com.asiankoala.koawalib

import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.math.radians
import kotlin.math.hypot
import kotlin.math.sqrt
import kotlin.test.Test

class VectorTest {
    @Test
    fun testAdding() {
        val v1 = Vector(2.0, 1.0)
        val v2 = Vector(1.0, 2.0)
        val sum = v1 + v2
        assert(sum.x, sum.y)
    }

    @Test
    fun testSubtracting() {
        val v1 = Vector()
        val v2 = Vector(-1.0, -2.0)
        val diff = v1 - v2
        assert(diff.x, 1.0)
        assert(diff.y, 2.0)
    }

    @Test
    fun testNorm() {
        val sqrt2 = sqrt(2.0)
        val v = Vector(1.0, 1.0)
        assert(sqrt2, v.norm)
    }

    @Test
    fun testDot() {
        val v1 = Vector(2.0, 3.0)
        val v2 = Vector(5.0, 6.0)
        assert(v1 dot v2, v2 dot v1)
        assert(v1 dot v1, v1.norm * v1.norm)
    }

    @Test
    fun testDist() {
        val v1 = Vector(2.0, 2.0)
        val v2 = Vector(2.0, 5.0)
        val v3 = Vector(5.0, 2.0)
        val d = 3.0
        assert(v1 dist v2, d)
        assert(v1 dist v3, d)
        val v4 = Vector()
        assert(v1 dist v4, hypot(2.0, 2.0))
    }

    @Test
    fun testUnaryMinus() {
        val v1 = Vector(1.0, 1.0)
        val v2 = Vector(-1.0, -1.0)
        val minus = -v1
        assert(minus.x, v2.x)
        assert(minus.y, v2.y)
    }

    @Test
    fun testAngle() {
        val v1 = Vector(1.0, 1.0)
        val v2 = -v1
        assert(v1.angle, 45.0.radians)
        assert(v2.angle, (45.0 + 180.0).radians.angleWrap)
    }
}