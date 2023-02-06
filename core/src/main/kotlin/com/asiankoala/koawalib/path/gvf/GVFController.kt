package com.asiankoala.koawalib.path.gvf

import com.asiankoala.koawalib.path.HermitePath
import com.asiankoala.koawalib.subsystem.drive.KMecanumOdoDrive

interface GVFController {
    val drive: KMecanumOdoDrive
    val path: HermitePath
    val isFinished: Boolean
    val disp: Double
    fun update()
}
