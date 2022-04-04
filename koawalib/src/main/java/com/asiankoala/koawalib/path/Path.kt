package com.asiankoala.koawalib.path

import com.asiankoala.koawalib.command.commands.PathCommand
import com.asiankoala.koawalib.math.*
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.subsystem.drive.KMecanumOdoDrive
import com.asiankoala.koawalib.util.Logger
import kotlin.math.absoluteValue

/**
 * Path movements should be continuous and fluid, and as such commands shouldn't adapt to the path,
 * rather the path should be adapted to the commands.
 */
class Path(private val waypoints: List<Waypoint>) {

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
            Logger.logInfo("attempting to run waypoint commands in interval [0,$currFollowIndex]")
            if (waypoint.command != null) {
                // TODO: WAS PREIVOUSLY !waypoint.command.isFinished && !waypoint.command.isScheduled, CHECK IF WORKS
                if (!waypoint.command.isScheduled) {
                    Logger.logInfo("scheduled waypoint $waypoint command ${waypoint.command}")
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
            movementLookahead.stop,
            movementLookahead.maxMoveSpeed,
            movementLookahead.maxTurnSpeed,
            movementLookahead.deccelAngle,
            movementLookahead.headingLockAngle,
            movementLookahead.slowDownTurnRadians,
            movementLookahead.lowestSlowDownFromTurnError,
        ).point

        val currFollowAngle = if (!waypoints[currFollowIndex].headingLockAngle.isNaN()) {
            waypoints[currFollowIndex].headingLockAngle
        } else {
            (turnLookahead.point - pose.point).atan2
        }

        val turnResult = PurePursuitController.pointTo(
            pose.heading,
            currFollowAngle,
            waypoints[currFollowIndex].maxTurnSpeed,
            waypoints[currFollowIndex].deccelAngle
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

        Logger.logInfo("pure pursuit debug started")
        Logger.logInfo("pose: $pose")
        Logger.logInfo("curr follow index: $currFollowIndex")
        Logger.logInfo("curr follow angle: ${currFollowAngle.degrees}")
        Logger.logInfo("move lookahead: $movementLookahead")
        Logger.logInfo("turn lookahead: $turnLookahead")
        Logger.logInfo("clipped distance: $clippedDistanceToEnd")
        Logger.logInfo("relative angle: ${realRelativeAngle.degrees}")
        Logger.logInfo("x power: $finalXPower")
        Logger.logInfo("y power: $finalYPower")
        Logger.logInfo("turn power: $finalTurnPower")
        Logger.logInfo("pure pursuit debug ended")
        return Pose(finalXPower, finalYPower, finalTurnPower)
    }

    fun inverted(): Path {
        val newWaypoints = ArrayList<Waypoint>()

        for (waypoint in waypoints) {
            newWaypoints.add(
                waypoint.copy(
                    y = -waypoint.y,
                    headingLockAngle = (waypoint.headingLockAngle + 180.0.radians).angleWrap
                )
            )
        }

        return Path(newWaypoints)
    }

    // integration with command scheduler
    fun schedule(drive: KMecanumOdoDrive) {
        PathCommand(drive, this).schedule()
    }
}
