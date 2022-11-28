package com.asiankoala.koawalib.path.gvf

import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.path.Path

interface GVFController {
    val path: Path
    val isFinished: Boolean
    val s: Double
    fun update(currPose: Pose): Pose
}
