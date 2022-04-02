package com.asiankoala.koawalib.path

import com.asiankoala.koawalib.math.*
import com.asiankoala.koawalib.math.Point
import com.asiankoala.koawalib.math.Pose

class GVFController(val path: GVFPath, val kOmega: Double, val kN: Double) {
    var lastT = 0.0001
    fun vectorAt(r: Point, closestT: Double): Point {
        return path.tangentVec(closestT)
            .minus(path.nVec(r, closestT).scale(kN * path.error(path.levelSet(r, closestT))))
    }

    fun headingControl(pose: Pose): Double {
        val closestT = path.closestTOnPathTo(pose, lastT)
        val curHeading_ = pose.directionVector()
        val curHeading = curHeading_.normalized()
        val vector = vectorAt(pose, closestT)
        val desiredHeadingVec = vector.normalized()
        val angleDelta = toHeading(desiredHeadingVec.atan2 - curHeading.atan2)
        lastT = closestT

        return kOmega * angleDelta
    }

    fun vectorControl(pose: Pose): Point {
        val closestT = path.closestTOnPathTo(pose, lastT)
        lastT = closestT
        val vector = vectorAt(pose, closestT)
        val desiredHeadingVec = vector.normalized()
        return desiredHeadingVec
    }
}