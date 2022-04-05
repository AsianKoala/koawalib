package com.asiankoala.koawalib.path

import com.asiankoala.koawalib.math.*
import com.asiankoala.koawalib.math.IndexPoint
import com.asiankoala.koawalib.math.Point
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.util.Logger
import com.qualcomm.robotcore.util.Range
import kotlin.math.*

@Suppress("unused")
object PurePursuitController {

    fun goToPosition(
        currPose: Pose,
        targetPosition: Point,
        stop: Boolean = false,
        maxMoveSpeed: Double = 1.0,
        maxTurnSpeed: Double = 1.0,
        deccelAngle: Double = 60.0.radians,
        headingLockAngle: Double? = null,
        minAllowedHeadingError: Double = 60.0.radians,
        lowestSlowDownFromHeadingError: Double = 0.4,
    ): Pose {
        val absoluteDelta = targetPosition - currPose.point
        val distanceToPoint = absoluteDelta.hypot

        val angleToPoint = absoluteDelta.atan2
        val deltaAngleToPoint = (angleToPoint - (currPose.heading - 90.0.radians)).angleWrap

        val relativeXToPosition = distanceToPoint * deltaAngleToPoint.cos
        val relativeYToPosition = distanceToPoint * deltaAngleToPoint.sin

        val relativeAbsMagnitude = hypot(relativeXToPosition, relativeYToPosition)
        var xPower = relativeXToPosition / relativeAbsMagnitude
        var yPower = relativeYToPosition / relativeAbsMagnitude

        if (stop) {
            xPower *= relativeXToPosition.absoluteValue / 12.0
            yPower *= relativeYToPosition.absoluteValue / 12.0
        } else {
            Logger.addTelemetryLine("FULL SPEED")
        }

        xPower = clamp(xPower, -maxMoveSpeed, maxMoveSpeed)
        yPower = clamp(yPower, -maxMoveSpeed, maxMoveSpeed)

        val absolutePointAngle = headingLockAngle ?: angleToPoint

        val pointRes = pointTo(absolutePointAngle, currPose.heading, maxTurnSpeed, deccelAngle)
        var turnPower = pointRes.first
        val relativePointAngle = pointRes.second.angleWrap

        if (distanceToPoint < 4.0) {
            turnPower = 0.0
        }

        val powers = mutableListOf(xPower, yPower, turnPower)
        val maxIdx = powers.indices.maxByOrNull { powers[it].absoluteValue }!!
        val highestPower = powers[maxIdx]
        powers[maxIdx] = absMax(0.1 * highestPower.sign, highestPower)
        xPower = powers[0]
        yPower = powers[1]
        turnPower = powers[2]

        xPower *= Range.clip(relativeXToPosition.absoluteValue / 2.5, 0.0, 1.0)
        yPower *= Range.clip(relativeYToPosition.absoluteValue / 2.5, 0.0, 1.0)

        // slow down if angle is off
        var errorTurnSoScaleMovement = Range.clip(
            1.0 - (relativePointAngle / minAllowedHeadingError).absoluteValue,
            lowestSlowDownFromHeadingError,
            1.0
        )

        if (turnPower.absoluteValue < 0.00001) {
            errorTurnSoScaleMovement = 1.0
        }

        xPower *= errorTurnSoScaleMovement
        yPower *= errorTurnSoScaleMovement

        return Pose(xPower, yPower, turnPower)
    }

    fun pointTo(
        targetAngle: Double,
        heading: Double,
        speed: Double,
        deccelAngle: Double
    ): Pair<Double, Double> {
        val relativePointAngle = (targetAngle - heading).angleWrap

        var turnSpeed = (relativePointAngle / deccelAngle) * speed
        turnSpeed = clamp(turnSpeed, -speed, speed)
        turnSpeed = absMax(0.1 * turnSpeed.sign, turnSpeed)
        turnSpeed *= clamp(relativePointAngle.absoluteValue / 3.0.radians, 0.0, 1.0)

        return Pair(turnSpeed, relativePointAngle)
    }


    /**
     * This will return which index along the path the robot is. It is the index of the first point
     * It also returns the point of the clipping
     */
    fun clipToPath(waypoints: List<Waypoint>, position: Point): IndexPoint {
        var closestClippedDistance = 100000.0
        var closestClippedIndex = 0
        var clippedToLine = Point()

        for (i in 0 until waypoints.size - 1) {
            val firstPoint = waypoints[i]
            val secondPoint = waypoints[i + 1]
            val currClippedToLine = clipToLine(firstPoint.point, secondPoint.point, position)
            val distanceToClip = (position - currClippedToLine).hypot
            if (distanceToClip < closestClippedDistance) {
                closestClippedDistance = distanceToClip
                closestClippedIndex = i
                clippedToLine = currClippedToLine
            }
        }

        return IndexPoint(clippedToLine, closestClippedIndex)
    }

    fun calcLookahead(waypoints: List<Waypoint>, currPose: Pose, followDistance: Double): Waypoint {

        // find what segment we're on
        val clippedToLine = clipToPath(waypoints, currPose.point)
        val currFollowIndex = clippedToLine.index + 1

        // extend circle, find intersects with segments, choose closest
        // to last point (TODO maybe heading based instead of waypoint order based ?)
        var followMe = waypoints[currFollowIndex].copy
        followMe = followMe.copy(x = clippedToLine.point.x, y = clippedToLine.point.y)
        var closestDistance = 69420.0
        for (i in 0 until waypoints.size - 1) {
            val startLine = waypoints[i]
            val endLine = waypoints[i + 1]

            val intersections = lineCircleIntersection(
                currPose.point,
                startLine.point,
                endLine.point,
                followDistance
            )

            for (intersection in intersections) {
                val dist = (intersection - waypoints[waypoints.size - 1].point).hypot

                if (dist < closestDistance) {
                    closestDistance = dist
                    followMe = followMe.copy(x = intersection.x, y = intersection.y)
//                    followMe = endLine.copy(x = intersection.x, y = intersection.y)
                    // TODO CHECK WHICH ONE WORKS BETTER
                }
            }
        }

        return followMe
    }
}
