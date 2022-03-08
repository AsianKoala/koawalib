package com.asiankoala.koawalib.path

import com.asiankoala.koawalib.math.IndexPoint
import com.asiankoala.koawalib.math.MathUtil
import com.asiankoala.koawalib.math.MathUtil.cos
import com.asiankoala.koawalib.math.MathUtil.radians
import com.asiankoala.koawalib.math.MathUtil.sin
import com.asiankoala.koawalib.math.MathUtil.wrap
import com.asiankoala.koawalib.math.Point
import com.asiankoala.koawalib.math.Pose
import com.qualcomm.robotcore.util.Range
import kotlin.math.*

// todo: clean up this class
object PurePursuitController {

    fun goToPosition(
        currPose: Pose,
        targetPosition: Point,
        followAngle: Double = 90.0.radians,
        stop: Boolean = false,
        maxMoveSpeed: Double = 1.0,
        maxTurnSpeed: Double = 1.0,
        isHeadingLocked: Boolean = false,
        headingLockAngle: Double = 0.0,
        slowDownTurnRadians: Double = 60.0.radians,
        lowestSlowDownFromTurnError: Double = 0.4,
        noTurn: Boolean = false
    ): Pose {

        val absoluteDelta = targetPosition - currPose.point
        val distanceToPoint = absoluteDelta.hypot

        val angleToPoint = absoluteDelta.atan2
        val deltaAngleToPoint = (angleToPoint - (currPose.heading - 90.0.radians)).wrap

        val relativeXToPosition = distanceToPoint * deltaAngleToPoint.cos
        val relativeYToPosition = distanceToPoint * deltaAngleToPoint.sin

        val relativeAbsMagnitude = hypot(relativeXToPosition, relativeYToPosition)
        var xPower = relativeXToPosition / relativeAbsMagnitude
        var yPower = relativeYToPosition / relativeAbsMagnitude

        println("raw x power $xPower")
        println("raw y power $yPower")

        if (stop) {
            xPower *= relativeXToPosition.absoluteValue / 12.0
            yPower *= relativeYToPosition.absoluteValue / 12.0
        } else {
            println("FULL SPEED")
        }

        xPower = MathUtil.clamp(xPower, -maxMoveSpeed, maxMoveSpeed)
        yPower = MathUtil.clamp(yPower, -maxMoveSpeed, maxMoveSpeed)

        val actualRelativePointAngle = (followAngle - 90.0.radians)

        val absolutePointAngle = if (isHeadingLocked) {
            headingLockAngle
        } else {
            angleToPoint + actualRelativePointAngle
        }

        val relativePointAngle = (absolutePointAngle - currPose.heading).wrap
        val deccelAngle = 45.0.radians

        var turnPower = (relativePointAngle / deccelAngle) * maxTurnSpeed
        turnPower = MathUtil.clamp(turnPower, -maxTurnSpeed, maxTurnSpeed)

        if (distanceToPoint < 4.0) {
            turnPower = 0.0
        }

        xPower *= Range.clip(relativeXToPosition.absoluteValue / 2.5, 0.0, 1.0)
        yPower *= Range.clip(relativeYToPosition.absoluteValue / 2.5, 0.0, 1.0)

        turnPower *= Range.clip(relativePointAngle.absoluteValue / 3.0.radians, 0.0, 1.0)

        println("close scalar Y ${Range.clip(relativeYToPosition.absoluteValue / 2.5, 0.0, 1.0)}")

        println("pre turn xPower $xPower")
        println("pre turn ypower $yPower")

        if (noTurn) {
            return Pose(xPower, yPower, 0.0)
        }

        // slow down if angle is off
        var errorTurnSoScaleMovement = Range.clip(1.0 - (relativePointAngle / slowDownTurnRadians).absoluteValue, lowestSlowDownFromTurnError, 1.0)

        if (turnPower.absoluteValue < 0.00001) {
            errorTurnSoScaleMovement = 1.0
        }

        println("error turn movment scale $errorTurnSoScaleMovement")

        xPower *= errorTurnSoScaleMovement
        yPower *= errorTurnSoScaleMovement

        println("relative X $relativeXToPosition")
        println("relative Y $relativeYToPosition")

        println("final x power goTopos $xPower")
        println("final y power goToPos $yPower")

        return Pose(xPower, yPower, turnPower)
    }

    fun clipToLine(start: Point, end: Point, robot: Point): Point {
        var startX = start.y
        var startY = start.x

        if (start.x == end.x)
            startX += 0.001

        if (start.y == end.y)
            startY += 0.001

        val mStart = Point(startX, startY)

        val m1 = (end.y - mStart.y) / (end.x - mStart.x)
        val m2 = -1.0 / m1
        val xClip = (-m2 * robot.x + robot.y + m1 * mStart.x - mStart.y) / (m1 - m2)
        val yClip = m1 * (xClip - mStart.x) + mStart.y
        return Point(xClip, yClip)
    }

    fun extendLine(firstPoint: Point, secondPoint: Point, distance: Double): Point {
        val lineAngle = (secondPoint - firstPoint).atan2
        val length = secondPoint.distance(firstPoint)
        val extendedLineLength = length + distance

        val extendedX = lineAngle.cos * extendedLineLength + firstPoint.x
        val extendedY = lineAngle.sin * extendedLineLength + firstPoint.y
        return Point(extendedX, extendedY)
    }

    /**
     * @param center          center point of circle
     * @param startPoint start point of the line segment
     * @param endPoint   end point of the line segment
     * @param radius     radius of the circle
     * @return intersection point closest to endPoint
     * @see [https://mathworld.wolfram.com/Circle-LineIntersection.html](https://mathworld.wolfram.com/Circle-LineIntersection.html)
     */
    private fun lineCircleIntersection(
        center: Point,
        startPoint: Point,
        endPoint: Point,
        radius: Double
    ): List<Point> {
        val start = startPoint - center
        val end = endPoint - center
        val deltas = end - start
        val d = start.x * end.y - end.x * start.y
        val discriminant = radius.pow(2) * deltas.hypot.pow(2) - d.pow(2)

        // discriminant = 0 for 1 intersection, >0 for 2
        val intersections = ArrayList<Point>()
        val xLeft = d * deltas.y
        val yLeft = -d * deltas.x
        val xRight: Double = MathUtil.stupidSign(deltas.y) * deltas.x * sqrt(discriminant)
        val yRight = deltas.y.absoluteValue * sqrt(discriminant)
        val div = deltas.hypot.pow(2)
        if (discriminant == 0.0) {
            intersections.add(Point(xLeft / div, yLeft / div))
        } else {
            // add 2 points, one with positive right side and one with negative right side
            intersections.add(Point((xLeft + xRight) / div, (yLeft + yRight) / div))
            intersections.add(Point((xLeft - xRight) / div, (yLeft - yRight) / div))
        }
//        var closest = Point(69420.0, -69420.0)
//        for (p in intersections) { // add circle center offsets
//            val offsetPoint = Point(p.x + center.x, p.y + center.y)
//            if (offsetPoint.distance(endPoint) < closest.distance(endPoint)) {
//                closest = offsetPoint
//            }
//        }
//        return closest
        return intersections.map { it + center }
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

    fun pointTo(heading: Double, angle: Double, speed: Double, deccelAngle: Double): Pair<Double, Double> {
        val relativePointAngle = (angle - heading).wrap

        var turnSpeed = (relativePointAngle / deccelAngle) * speed
        turnSpeed = MathUtil.clamp(turnSpeed, -speed, speed)

        turnSpeed *= MathUtil.clamp(relativePointAngle.absoluteValue / 3.0.radians, 0.0, 1.0)
        return Pair(turnSpeed, relativePointAngle)
    }

    fun calcLookahead(waypoints: List<Waypoint>, currPose: Pose, followDistance: Double): Waypoint {

        // find what segment we're on
        val clippedToLine = clipToPath(waypoints, currPose.point)
        val currFollowIndex = clippedToLine.index + 1

        // extend circle, find intersects with segments, choose closest
        // to last point (TODO maybe heading based instead of waypoint order based ?)
        var followMe = waypoints[currFollowIndex].copy
        followMe = followMe.copy(x = clippedToLine.point.x, y = clippedToLine.point.y)

        for (i in 0 until waypoints.size - 1) {
            val startLine = waypoints[i]
            val endLine = waypoints[i + 1]

            val intersections = lineCircleIntersection(currPose.point, startLine.point, endLine.point, followDistance)

            var closestDistance = 69420.0
            for (intersection in intersections) {
                val dist = (intersection - waypoints[waypoints.size - 1].point).hypot

                if (dist < closestDistance) {
                    closestDistance = dist
//                    followMe = followMe.copy(x = intersection.x, y = intersection.y)
                    followMe = endLine.copy(x = intersection.x, y = intersection.y)
                }
            }
        }

        return followMe
    }
}
