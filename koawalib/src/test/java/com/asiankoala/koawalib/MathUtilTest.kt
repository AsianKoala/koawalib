package com.asiankoala.koawalib

import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.clipToLine
import com.asiankoala.koawalib.math.extendLine
//import com.asiankoala.koawalib.math.newExtendLine
import com.asiankoala.koawalib.math.radians

object MathUtilTest {
    @JvmStatic
    fun main(args: Array<String>) {
        lineProjectTest()
        oldLineProjectTest()
    }

    fun lineExtendTest() {
        val start = Vector()
        val end = Vector(10.0, 10.0)
        val d = Vector(5.0, 5.0).norm
        val firstExtend = extendLine(start, end, d)
//        val secondExtend = newExtendLine(start, end, d)
        println(firstExtend)
//        println(secondExtend)
    }

    fun lineProjectTest() {
        val v = Vector(2.0, 3.0)
        val s = Vector(1.0, 2.0)
        val proj = s * ((v dot s) / (s dot s))
        println(proj)
    }

    fun oldLineProjectTest() {
        val v = Vector(2.0, 3.0)
        val s = Vector()
        val s2 = Vector(1.0, 2.0)
        val proj = clipToLine(s, s2, v)
        println(proj)
    }
}