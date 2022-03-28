package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.math.*
import com.asiankoala.koawalib.math.Point
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.TimePose
import kotlin.math.absoluteValue

interface Localized {
    val position: Pose
    val velocity: Pose
    val prevRobotRelativePositions: ArrayList<TimePose>
    var robotRelativeMovement: Pose

    fun localize()
    fun updateTelemetry()

    fun updatePoseWithDeltas(currPose: Pose, lWheelDelta: Double, rWheelDelta: Double, dx: Double, dy: Double, angleIncrement: Double): Point {
        var deltaX = dx
        var deltaY = dy
        if (angleIncrement.absoluteValue > 0) {
            val radiusOfMovement = (lWheelDelta + rWheelDelta) / (2 * angleIncrement)
            val radiusOfStrafe = deltaX / angleIncrement

            deltaX = (radiusOfMovement * (1 - angleIncrement.cos)) + (radiusOfStrafe * angleIncrement.sin)
            deltaY = (radiusOfMovement * angleIncrement.sin) + (radiusOfStrafe * (1 - angleIncrement.cos))
        }

        val robotDeltaRelativeMovement = Pose(deltaX, deltaY, angleIncrement)
        robotRelativeMovement = robotRelativeMovement.plusWrap(robotDeltaRelativeMovement)

        prevRobotRelativePositions.add(TimePose(robotRelativeMovement))

        val incrementX = currPose.heading.cos * deltaY - currPose.heading.sin * deltaX
        val incrementY = currPose.heading.sin * deltaY + currPose.heading.cos * deltaX
        return Point(incrementX, incrementY)
    }
}
