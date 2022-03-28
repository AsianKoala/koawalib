package com.asiankoala.koawalib.path

import com.asiankoala.koawalib.command.commands.PathCommand
import com.asiankoala.koawalib.math.*
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.subsystem.drive.KMecanumOdoDrive
import com.asiankoala.koawalib.util.Logger
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
            Logger.logDebug("attempting to run waypoint commands in interval [0,$currFollowIndex]")
            if (waypoint.command != null) {
                // TODO: WAS PREIVOUSLY !waypoint.command.isFinished && !waypoint.command.isScheduled
                // TODO: CHECK IF NEW WORKS
                if (!waypoint.command.isScheduled) {
                    Logger.logDebug("scheduled waypoint $waypoint command ${waypoint.command}")
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

        extendedPath[waypoints.size - 1] =
            extendedPath[waypoints.size - 1].copy(x = last.x, y = last.y)

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
            noTurn = true,
            shouldTelemetry = false
        ).point

        val currFollowAngle = if (waypoints[currFollowIndex].isHeadingLocked) {
            waypoints[currFollowIndex].headingLockAngle
        } else {
            val absoluteAngle = (turnLookahead.point - pose.point).atan2
            (absoluteAngle + followAngle - 90.0.radians).wrap
        }

        val turnResult = PurePursuitController.pointTo(
            pose.heading,
            currFollowAngle,
            waypoints[currFollowIndex].maxTurnSpeed,
            45.0.radians
        )
        val finalTurnPower = turnResult.first
        val realRelativeAngle = turnResult.second

        val errorTurnSoScaleMovement = clamp(
            1.0 - (realRelativeAngle / movementLookahead.slowDownTurnRadians).absoluteValue,
            movementLookahead.lowestSlowDownFromTurnError,
            1.0
        )

        val finalXPower = movePower.x * errorTurnSoScaleMovement
        val finalYPower = movePower.y * errorTurnSoScaleMovement

        if (clippedDistanceToEnd < 1.0) {
            isFinished = true
        }

        Logger.logDebug("pure pursuit debug started")
        Logger.logDebug("pose: $pose")
        Logger.logDebug("curr follow index: $currFollowIndex")
        Logger.logDebug("curr follow angle: ${currFollowAngle.degrees}")
        Logger.logDebug("move lookahead: $movementLookahead")
        Logger.logDebug("turn lookahead: $turnLookahead")
        Logger.logDebug("clipped distance: $clippedDistanceToEnd")
        Logger.logDebug("relative angle: ${realRelativeAngle.degrees}")
        Logger.logDebug("x power: $finalXPower")
        Logger.logDebug("y power: $finalYPower")
        Logger.logDebug("turn power: $finalTurnPower")
        Logger.logDebug("pure pursuit debug ended")
        return Pose(finalXPower, finalYPower, finalTurnPower)
    }

    fun inverted(): Path {
        val newWaypoints = ArrayList<Waypoint>()

        for (waypoint in waypoints) {
            newWaypoints.add(
                waypoint.copy(
                    y = -waypoint.y,
                    headingLockAngle = (waypoint.headingLockAngle + 180.0.radians).wrap
                )
            )
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
        PathCommand(drive, this).schedule()
    }
}
