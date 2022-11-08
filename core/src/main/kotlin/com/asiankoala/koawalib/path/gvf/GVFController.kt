package com.asiankoala.koawalib.path.gvf

import com.asiankoala.koawalib.math.Pose

interface GVFController {
    val isFinished: Boolean
    fun update(currPose: Pose): Pose
}
