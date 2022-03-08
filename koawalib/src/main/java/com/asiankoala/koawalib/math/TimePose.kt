package com.asiankoala.koawalib.math

import com.asiankoala.koawalib.math.Pose

data class TimePose(val pose: Pose, val timestamp: Long = System.currentTimeMillis())
