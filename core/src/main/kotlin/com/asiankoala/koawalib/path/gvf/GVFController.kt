package com.asiankoala.koawalib.path.gvf

import com.asiankoala.koawalib.path.TangentPath
import com.asiankoala.koawalib.subsystem.drive.KMecanumOdoDrive

interface GVFController {
    val drive: KMecanumOdoDrive
    val path: TangentPath
    val isFinished: Boolean
    val s: Double
    fun update()
}
