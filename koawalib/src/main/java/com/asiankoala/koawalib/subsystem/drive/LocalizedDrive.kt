package com.asiankoala.koawalib.subsystem.drive

import com.asiankoala.koawalib.math.Pose

interface LocalizedDrive {
    val pose: Pose
    val vel: Pose
}