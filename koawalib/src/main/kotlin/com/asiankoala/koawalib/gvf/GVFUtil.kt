package com.asiankoala.koawalib.gvf

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.path.Path
import com.asiankoala.koawalib.math.Vector
import kotlin.math.PI
import kotlin.math.sign

internal object GVFUtil {
    fun Vector2d.toVec() = Vector(this.x, this.y)

    fun gvfVecAt(path: Path, pose: Pose2d, s: Double, kN: Double, errorMap: (Double) -> Double): Vector2d {
        val tangentVec = path.deriv(s).vec()
        val normalVec = tangentVec.rotated(PI / 2.0)
        val projected = path[s].vec()
        val displacementVec = projected - pose.vec()
        val orientation = displacementVec.toVec() cross tangentVec.toVec()
        val error = displacementVec.norm() * orientation.sign
        return tangentVec - normalVec * kN * errorMap.invoke(error)
    }
}
