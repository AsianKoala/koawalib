package com.asiankoala.koawalib.util

import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.radians
import kotlin.test.Test

class SpeedsTest {
    @Test
    fun testFieldCentricSet() {
        val powers = Pose(1.0, 1.0, 0.0)
        val speeds = Speeds()
        speeds.setFieldCentric(powers)
        val fieldCentric = speeds.getFieldCentric()
        com.asiankoala.koawalib.assert(powers.asList, fieldCentric.asList)
    }

    @Test
    fun testRobotCentricSet() {
        val powers = Pose(1.0, 1.0, 0.0)
        val speeds = Speeds()
        speeds.setRobotCentric(powers, 90.0.radians)
        var fieldCentric = speeds.getFieldCentric()
        var expected = Pose(1.0, 1.0, 0.0)
        com.asiankoala.koawalib.assert(expected.asList, fieldCentric.asList)

        val oneMag = Vector(1.0, 0.0)
        val v45 = oneMag.rotate(45.0.radians)
        speeds.setRobotCentric(Pose(oneMag, 0.0), 45.0.radians)
        fieldCentric = speeds.getFieldCentric()
        expected = Pose(v45, 0.0)
        com.asiankoala.koawalib.assert(expected.asList, fieldCentric.asList)
    }

    @Test
    fun testFieldCentricGet() {
        val powers = Pose(1.0, 1.0, 1.0)
        val speeds = Speeds()
        speeds.setFieldCentric(powers)
        val fieldCentric = speeds.getFieldCentric()
        com.asiankoala.koawalib.assert(powers.asList, fieldCentric.asList)
    }

    @Test
    fun testRobotCentricGet() {
        val powers = Pose(Vector(1.0, 1.0), 1.0)
        val speeds = Speeds()
        speeds.setFieldCentric(powers)
        val robotCentric = speeds.getRobotCentric(45.0.radians)
        val expected = Pose(Vector(y = 1.0) * powers.vec.norm, 1.0)
        com.asiankoala.koawalib.assert(expected.asList, robotCentric.asList)
    }

    @Test
    fun testWheelGet() {
        val heading = 90.0.radians
        val powers = Pose(y = 1.0)
        val speeds = Speeds()
        speeds.setFieldCentric(powers)
        val wheels = speeds.getWheels(heading)
        val expected = listOf(1.0, 1.0, 1.0, 1.0)
        com.asiankoala.koawalib.assert(wheels, expected)
    }
}