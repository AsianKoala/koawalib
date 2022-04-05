package com.asiankoala.koawalib

object SplineInterpolationTest {
    @JvmStatic
    fun main(args: Array<String>) {
//        val path = SplinePath(arrayOf(
//            Pose(0.0, 0.0, 0.0),
//            Pose(5.0, 5.0, PI / 2),
//            Pose(10.0, 10.0, 0.0),
//            Pose(20.0, 10.0, 0.0),
//            Pose(25.0, 5.0, 0.0),
//            Pose(40.0, -20.0, 0.0.radians),
//            Pose(10.0, -20.0, 0.0.radians)
//        ))
//        println(path.length())
//        val controller = GVFController(path, 3.0, 3.0)
//        val dt = 10.0 / 1000.0
//
//        val xRobot = ArrayList<Double>()
//        val yRobot = ArrayList<Double>()
//        var position = Point(0.00, -0.0)
//        var heading = Point.fromAngle(0.0 * PI / 2)
//        val time = ElapsedTime()
//
//        while(time.seconds() < 5.0) {
//            val pose = Pose(position.x, position.y, heading.atan2)
//            heading = controller.vectorControl(pose)
//            position += heading.scale(dt)
//
//            xRobot.add(position.x)
//            yRobot.add(position.y)
//        }
//
//        val xPath = DoubleArray(201)
//        val yPath = DoubleArray(201)
//        for (i in 0..200) {
//            val t = i / 205.0
//            val xy = path.calculatePoint(t)
//            xPath[i] = xy.x
//            yPath[i] = xy.y
//        }
//        println("$time")
//
//        plot(xPath, yPath)
    }
//
}