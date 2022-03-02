package com.asiankoala.koawalib.path

import com.asiankoala.koawalib.command.commands.PathCommand
import com.asiankoala.koawalib.math.MathUtil
import com.asiankoala.koawalib.math.MathUtil.degrees
import com.asiankoala.koawalib.math.MathUtil.radians
import com.asiankoala.koawalib.math.MathUtil.wrap
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.subsystem.drive.KMecanumOdoDrive
import kotlin.math.absoluteValue

/**
 * To be as fast as possible during auto, we only want to run commands that DO NOT interrupt
 * when the next waypoint is targeted (CommandWaypoint) OR commands at the start/end of each path.
 * Path movements should be continuous and fluid, and as such commands shouldn't adapt to the path,
 * rather the path should be adapted to the commands.
 *
 * @param followAngle - dynamic angle to follow path with. 90 degrees = forward, <90 = turning right, >90 = turning left
 */
class Path(private val waypoints: List<Waypoint>, private val followAngle: Double = 90.0.radians) {

    var isFinished = false
        private set

    fun update(pose: Pose): Pose {
        val extendedPath = ArrayList<Waypoint>(waypoints)

        val clippedToPath = PurePursuitController.clipToPath(waypoints, pose.point)
        val currFollowIndex = clippedToPath.index + 1

        // NOTE: we start running commands based on CLIPPED position
        // meaning, if the robot hasn't passed a waypoint, even if following that next waypoint's segment
        // the robot will not run the next waypoint command until after it has passed it (reflected by currFollowIndex)
        for (waypoint in waypoints.subList(0, currFollowIndex)) {
            if (waypoint.command != null) {
                if (!waypoint.command.isFinished && !waypoint.command.isScheduled) {
                    waypoint.command.schedule()
                }
            }
        }

        var movementLookahead = PurePursuitController.calcLookahead(
            waypoints,
            pose,
            waypoints[currFollowIndex].followDistance
        )

        val last = PurePursuitController.extendLine(
            waypoints[waypoints.size - 2].point,
            waypoints[waypoints.size - 1].point,
            waypoints[waypoints.size - 1].turnLookaheadDistance * 1.5
        )

        extendedPath[waypoints.size - 1] = extendedPath[waypoints.size - 1].copy(x = last.x, y = last.y)

        val turnLookahead = PurePursuitController.calcLookahead(
            extendedPath,
            pose,
            waypoints[currFollowIndex].turnLookaheadDistance
        )

        val clippedDistanceToEnd = (clippedToPath.point - waypoints[waypoints.size - 1].point).hypot

        if (clippedDistanceToEnd <= movementLookahead.followDistance + 6 ||
            (pose.point - waypoints[waypoints.size - 1].point).hypot < movementLookahead.followDistance + 6
        ) {
            movementLookahead = waypoints[waypoints.size - 1]
        }

        val movePower = PurePursuitController.goToPosition(
            pose,
            movementLookahead.point,
            followAngle,
            movementLookahead.stop,
            movementLookahead.maxMoveSpeed,
            movementLookahead.maxTurnSpeed,
            movementLookahead.isHeadingLocked,
            movementLookahead.headingLockAngle,
            movementLookahead.slowDownTurnRadians,
            movementLookahead.lowestSlowDownFromTurnError,
            true
        ).point

        val currFollowAngle = if (waypoints[currFollowIndex].isHeadingLocked) {
            waypoints[currFollowIndex].headingLockAngle
        } else {
            val absoluteAngle = (turnLookahead.point - pose.point).atan2
            (absoluteAngle + followAngle - 90.0.radians).wrap
        }

        val result = PurePursuitController.pointTo(
            pose.heading,
            currFollowAngle,
            waypoints[currFollowIndex].maxTurnSpeed,
            45.0.radians
        )
        val finalTurnPower = result.first
        val realRelativeAngle = result.second

        val errorTurnSoScaleMovement = MathUtil.clamp(1.0 - (realRelativeAngle / movementLookahead.slowDownTurnRadians).absoluteValue, movementLookahead.lowestSlowDownFromTurnError, 1.0)

        val finalXPower = movePower.x * errorTurnSoScaleMovement
        val finalYPower = movePower.y * errorTurnSoScaleMovement

        if (clippedDistanceToEnd < 1.0) {
            isFinished = true
        }

        println("pose: $pose")
        println("curr follow index: $currFollowIndex")
        println("curr follow angle: ${currFollowAngle.degrees}")
        println("move lookahead: $movementLookahead")
        println("turn lookahead: $turnLookahead")
        println("clipped distance: $clippedDistanceToEnd")
        println("relative angle: ${realRelativeAngle.degrees}")
        println("x power: $finalXPower")
        println("y power: $finalYPower")
        println("turn power: $finalTurnPower")
        return Pose(finalXPower, finalYPower, finalTurnPower)
    }

    fun inverted(): Path {
        val newWaypoints = ArrayList<Waypoint>()

        for (waypoint in waypoints) {
            newWaypoints.add(waypoint.copy(y = -waypoint.y, headingLockAngle = (waypoint.headingLockAngle + 180.0.radians).wrap))
        }

        val deltaFollowAngle = (90.0.radians - followAngle).wrap.absoluteValue

        val newFollowAngle = if (followAngle < 90.0.radians) {
            (90.0 + deltaFollowAngle).wrap
        } else {
            (90.0 - deltaFollowAngle).wrap
        }

        return Path(newWaypoints, newFollowAngle)
    }

    // integration with command scheduler
    fun schedule(drive: KMecanumOdoDrive) {
        PathCommand(drive, this)
    }
}
