package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.math.*
import com.asiankoala.koawalib.subsystem.DeviceSubsystem
import com.asiankoala.koawalib.util.Logger
import kotlin.math.absoluteValue
import kotlin.math.max

abstract class Odometry : DeviceSubsystem() {
    abstract fun updateTelemetry()
    abstract fun reset()
    private val prevRobotRelativePositions: ArrayList<TimePose> = ArrayList()
    private var robotRelativeMovement: Pose = Pose()
    var shouldReset = true
    var pose = Pose()
        protected set

    var startPose: Pose = Pose()
        set(value) {
            pose = value
            field = value
            Logger.logInfo("set start pose to $value")
        }

    val velocity: Pose
        get() {
            if (prevRobotRelativePositions.size < 2) {
                return Pose()
            }

            val oldIndex = max(0, prevRobotRelativePositions.size - 5 - 1)
            val old = prevRobotRelativePositions[oldIndex]
            val curr = prevRobotRelativePositions[prevRobotRelativePositions.size - 1]

            val scalar = (curr.timestamp - old.timestamp).toDouble() / 1000.0

            val dirVel = (curr.pose.point - old.pose.point).scalarDiv(scalar)
            val angularVel = (curr.pose.heading - old.pose.heading) * (1 / scalar)

            return Pose(dirVel, angularVel.angleWrap)
        }

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

        val incrementX = currPose.heading.cos * deltaY + currPose.heading.sin * deltaX
        val incrementY = currPose.heading.sin * deltaY - currPose.heading.cos * deltaX

        return Point(incrementX, incrementY)
    }
}
