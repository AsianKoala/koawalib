package com.asiankoala.koawalib.path.pp

import com.acmerobotics.dashboard.canvas.Canvas
import com.asiankoala.koawalib.control.controller.PIDFController
import com.asiankoala.koawalib.control.controller.PIDGains
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.math.lineCircleIntersection
import com.asiankoala.koawalib.path.*
import com.asiankoala.koawalib.subsystem.drive.KMecanumOdoDrive
import com.qualcomm.robotcore.util.ElapsedTime
import kotlin.math.*

class PurePursuitPath(
    private val drive: KMecanumOdoDrive,
    private var waypoints: List<Waypoint>,
    transGains: PIDGains,
    rotGains: PIDGains,
    private val closeExponent: Double = 1.0 / 6.0,
    private val undershootDistance: Double = 6.0,
    private val switchDistance: Double = 18.0
) {
    private var index = 0
    private val deadmanTimer = ElapsedTime()
    private val deadManSwitch = 2000.0
    private val xController = PIDFController(transGains)
    private val yController = PIDFController(transGains)
    private val rController = PIDFController(rotGains).apply {
        setInputBounds(-PI, PI)
    }
    val isFinished get() = index >= waypoints.size - 1

    private fun setTarget(controller: PIDFController, curr: Double, target: Double): Double {
        controller.targetPosition = target
        return controller.update(curr)
    }

    private fun zoom(tx: Double, ty: Double, th: Double): Pose {
        val trans = Vector(
            setTarget(xController, drive.pose.x, tx),
            setTarget(yController, drive.pose.y, ty),
        )
        val rot = setTarget(rController, drive.pose.heading, th)
        return Pose(
            trans.rotate(PI / 2.0 - drive.pose.heading),
            rot
        )
    }

    private fun goToPosition(target: Waypoint, end: StopWaypoint?) {
        val delta = drive.pose.vec.dist(target.vec)
        drive.powers = if(end == null || delta < switchDistance) {
            zoom(
                target.vec.x,
                target.vec.y,
                if(target is HeadingControlledWaypoint) target.h else (target.vec - drive.pose.vec).angle
            )
        } else if(delta > undershootDistance) { // need to incporporate robot velocity later.
            val t = lineCircleIntersection(end.vec, drive.pose.vec, end.vec, undershootDistance)
            zoom(t.x, t.y, end.h)
        } else {
            listOf(
                target.vec.x - drive.pose.x,
                target.vec.y - drive.pose.y,
                (end.h - drive.pose.heading).angleWrap
            ).map {
                abs(it).pow(closeExponent) * sign(it)
            }.let {
                Pose(
                    Vector(
                        it[0],
                        it[1]
                    ).rotate(PI / 2.0 - drive.pose.heading),
                    it[2]
                )
            }
        }
    }

    private fun project(v: Vector, onto: Vector): Vector {
        return onto * ((v dot onto) / (onto dot onto))
    }

    private fun trackToLine(start: Waypoint, end: Waypoint) {
        val proj = project(drive.pose.vec, end.vec - start.vec)
        val inter = lineCircleIntersection(proj, start.vec, end.vec, end.follow)
        end.vec = inter
        goToPosition(end, if(end is StopWaypoint) end else null)
    }

    fun flip() {
        waypoints = waypoints.map(Waypoint::flip)
    }

    fun update() {
        var skip = false
        var target = waypoints[index + 1]

        // Stop waypoint deadman switch
        if (target is StopWaypoint && deadmanTimer.milliseconds() > deadManSwitch) {
            skip = true
        } else if (target !is StopWaypoint || drive.vel.vec.norm > 1.0) {
            deadmanTimer.reset()
        }
        if (target is StopWaypoint) {
            if (drive.pose.vec.dist(target.vec) < target.epsilon && (drive.pose.heading - target.h).angleWrap < target.thetaEpsilon) {
                skip = true
            }
        } else {
            if (drive.pose.vec.dist(target.vec) < target.follow) {
                skip = true
            }
        }

        if (skip) {
            index++
            target = waypoints[index + 1]
            target.cmd?.schedule()
        }

        if(isFinished) return

        if (target is StopWaypoint && drive.pose.vec.dist(target.vec) < target.follow) {
            goToPosition(target, target)
        } else {
            trackToLine(waypoints[index], target)
        }
    }

    fun draw(t: Canvas): Canvas {
        val xPoints = DoubleArray(waypoints.size)
        val yPoints = DoubleArray(waypoints.size)
        for (i in waypoints.indices) {
            xPoints[i] = waypoints[i].vec.x
            yPoints[i] = waypoints[i].vec.y
        }
        return t.setStroke("red").setStrokeWidth(1).strokePolyline(xPoints, yPoints)
    }
}