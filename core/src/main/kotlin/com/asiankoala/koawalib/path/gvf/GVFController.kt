package com.asiankoala.koawalib.path.gvf

import com.asiankoala.koawalib.math.Pose

interface GVFController {
    val isFinished: Boolean
    val s: Double
    fun update(currPose: Pose): Pose
}
