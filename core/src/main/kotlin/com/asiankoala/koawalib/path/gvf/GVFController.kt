package com.asiankoala.koawalib.path.gvf

import com.asiankoala.koawalib.path.Path
import com.asiankoala.koawalib.subsystem.drive.KMecanumOdoDrive

interface GVFController {
    val drive: KMecanumOdoDrive
    val path: Path
    val isFinished: Boolean
    val s: Double
    fun update()
}
