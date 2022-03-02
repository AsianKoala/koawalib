package com.asiankoala.koawalib.subsystem.odometry

import com.asiankoala.koawalib.math.Pose

interface Localized {
    val position: Pose
    val velocity: Pose

    fun localize()
}
