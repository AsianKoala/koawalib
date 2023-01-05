package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.math.*
import com.asiankoala.koawalib.subsystem.Subsystem
import com.asiankoala.koawalib.util.Speeds
import kotlin.math.*

abstract class Odometry(
    protected var startPose: Pose,
) : Subsystem() {
    internal data class TimePose(val pose: Pose, val timestamp: Long = System.currentTimeMillis())

    abstract fun updateTelemetry()
    abstract fun reset(p: Pose)
    private val prev: ArrayList<TimePose> = ArrayList()
    var pose = startPose
        protected set

    val velocity: Pose
        get() {
            if (prev.size < 2) return Pose()
            val oldIndex = max(0, prev.size - 5 - 1)
            val old = prev[oldIndex]
            val curr = prev[prev.size - 1]
            val scalar = (curr.timestamp - old.timestamp).toDouble() / 1000.0
            val dirVel = (curr.pose.vec - old.pose.vec) / scalar
            val angularVel = (curr.pose.heading - old.pose.heading) * (1 / scalar)
            return Pose(dirVel, angularVel.angleWrap)
        }
    private fun rot(v: Vector, s: Double, c: Double) =
        Vector(s * v.x - c * v.y, c * v.x + s * v.y)

    protected fun exp(global: Pose, inc: Pose): Pose {
        val u = inc.heading + inc.heading.sign * EPSILON
        val s = sin(u) / u
        val c = (1.0 - cos(u)) / u
        val trans = rot(inc.vec, s, c)
        val theta = global.heading + inc.heading
        val delta = rot(trans, sin(theta), cos(theta))
        prev.add(prev.lastOrNull()?.let {
            TimePose(Pose(it.pose.vec + trans, it.pose.heading + inc.heading))
        } ?: TimePose(global))
        return Pose(global.vec + delta, theta)
    }

    companion object {
        var lastPose = Pose()
            private set
    }
}
