package com.asiankoala.koawalib.hardware

import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.project
import kotlin.math.PI
import kotlin.math.max
import kotlin.test.Test

class SwerveTest {
    private val motor1 = Vector(1.0, 1.0).unit
    private val motor2 = Vector(-1.0, 1.0).unit
    private val fullRotation = Vector(0.0, 1.0)
    private val fullTranslation = Vector(1.0, 0.0)

    @Test
    fun testVectorProjection() {
        var motor1Vec = project(fullTranslation, motor1)
        var motor2Vec = project(fullTranslation, motor2)

        if(motor1Vec.norm > 1.0 || motor2Vec.norm > 1.0) {
            val m = max(motor1Vec.norm, motor2Vec.norm)
            motor1Vec /= m
            motor2Vec /= m
        }

        println(motor1Vec)
        println(motor2Vec)
    }

    @Test
    fun testVelocity() {
        val currentMotor1 = project(fullTranslation, motor1)
        val currentMotor2 = project(fullTranslation, motor2)
    }
}