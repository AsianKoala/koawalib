package com.asiankoala.koawalib.gvf

import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.path.Path
import com.asiankoala.koawalib.math.Pose

class MPGVFController(
    path: Path,
    kN: Double,
    kOmega: Double,
    kTheta: Double,
    kF: Double,
    epsilon: Double,
    errorMap: (Double) -> Double = { it },
) : GVFController(path, kN, kOmega, kTheta, kF, epsilon, errorMap) {
    override fun headingControl(vararg params: Any): Pair<Double, Double> {
        TODO("Not yet implemented")
    }

    override fun vectorControl(vararg params: Any): Vector2d {
        TODO("Not yet implemented")
    }

    override fun update(currPose: Pose): Pose {
        TODO("Not yet implemented")
    }
}