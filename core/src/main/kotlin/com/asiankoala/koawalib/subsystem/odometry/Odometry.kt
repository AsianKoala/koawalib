package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.math.*
import com.asiankoala.koawalib.subsystem.Subsystem
import com.asiankoala.koawalib.util.Speeds
import kotlin.math.absoluteValue
import kotlin.math.max

abstract class Odometry(
    protected var startPose: Pose,
) : Subsystem() {
    internal data class TimePose(val pose: Pose, val timestamp: Long = System.currentTimeMillis())

    abstract fun updateTelemetry()
    abstract fun reset()
    private val prevRobotRelativePositions: ArrayList<TimePose> = ArrayList()
    private var robotRelativeMovement: Pose = Pose()
    var pose = startPose
        protected set

    val velocity: Pose
        get() {
            if (prevRobotRelativePositions.size < 2) {
                return Pose()
            }

            val oldIndex = max(0, prevRobotRelativePositions.size - 5 - 1)
            val old = prevRobotRelativePositions[oldIndex]
            val curr = prevRobotRelativePositions[prevRobotRelativePositions.size - 1]

            val scalar = (curr.timestamp - old.timestamp).toDouble() / 1000.0

            val dirVel = (curr.pose.vec - old.pose.vec) / scalar
            val angularVel = (curr.pose.heading - old.pose.heading) * (1 / scalar)

            return Pose(dirVel, angularVel.angleWrap)
        }

    fun fieldCentricVelocity(heading: Double): Pose {
        val s = Speeds()
        s.setRobotCentric(velocity, heading)
        return s.getFieldCentric()
    }

    protected fun savePose(p: Pose) {
        lastPose = p
    }

    protected fun updatePoseWithDeltas(currPose: Pose, lWheelDelta: Double, rWheelDelta: Double, dx: Double, dy: Double, angleIncrement: Double): Vector {
        var deltaX = dx
        var deltaY = dy
        if (angleIncrement.absoluteValue > 0) {
            val radiusOfMovement = (lWheelDelta + rWheelDelta) / (2 * angleIncrement)
            val radiusOfStrafe = deltaX / angleIncrement

            deltaX = (radiusOfMovement * (1 - angleIncrement.cos)) + (radiusOfStrafe * angleIncrement.sin)
            deltaY = (radiusOfMovement * angleIncrement.sin) + (radiusOfStrafe * (1 - angleIncrement.cos))
        }

        val robotDeltaRelativeMovement = Pose(deltaX, deltaY, angleIncrement)
//        robotRelativeMovement = robotRelativeMovement.plusWrap(robotDeltaRelativeMovement)
        robotRelativeMovement = Pose(
            robotRelativeMovement.vec + robotDeltaRelativeMovement.vec,
            (robotRelativeMovement.heading + robotDeltaRelativeMovement.heading).angleWrap
        )
        prevRobotRelativePositions.add(TimePose(robotRelativeMovement))

        val incrementX = currPose.heading.cos * deltaY + currPose.heading.sin * deltaX
        val incrementY = currPose.heading.sin * deltaY - currPose.heading.cos * deltaX
        return Vector(incrementX, incrementY)
    }

    companion object {
        var lastPose = Pose()
            private set
    }
}
