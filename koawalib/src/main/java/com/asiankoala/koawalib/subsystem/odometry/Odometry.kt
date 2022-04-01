package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.math.*
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.TimePose
import com.asiankoala.koawalib.subsystem.DeviceSubsystem
import kotlin.math.max

abstract class Odometry : DeviceSubsystem(), Localized {
    protected var _position = Pose()
    override val position: Pose get() = _position

    override val velocity: Pose
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

            return Pose(dirVel, angularVel.wrap)
        }

    internal var startPose = Pose()
        set(value) {
            _position = value
            field = value
        }

    override val prevRobotRelativePositions = ArrayList<TimePose>()
    override var robotRelativeMovement = Pose()
}
