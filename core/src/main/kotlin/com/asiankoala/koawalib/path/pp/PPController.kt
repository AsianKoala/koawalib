package com.asiankoala.koawalib.path.pp

import com.asiankoala.koawalib.path.HermitePath
import com.asiankoala.koawalib.subsystem.drive.KMecanumDrive

class PPController(
    private val drive: KMecanumDrive,
    private val path: HermitePath,
    private val lookahead: Double,
) {
}