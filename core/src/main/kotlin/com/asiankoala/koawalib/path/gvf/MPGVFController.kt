package com.asiankoala.koawalib.path.gvf

import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.math.degrees
import com.asiankoala.koawalib.path.Path
import com.asiankoala.koawalib.util.Speeds
import kotlin.math.min

class MPGVFController(
    path: Path,
    kN: Double,
    kOmega: Double,
    epsilon: Double,
    thetaEpsilon: Double,
    constraints: Constraints
    errorMap: (Double) -> Double = { it },
) : GVFController(path, kN, kOmega, epsilon, thetaEpsilon, errorMap) {
    private val profile = OnlineProfile(
        DispState(),
        DispState(path.length, 0.0, 0.0),
        constraints
    )

    override fun headingControl(vel: Speeds): Pair<Double, Double> {
        val error = (path[s].heading - pose.heading).angleWrap.degrees
        val result = kOmega * error
        return Pair(result, error)
    }

    override fun vectorControl(vel: Speeds): Vector {
        val state = profile[s]
        // let g(s) be our gvf function, p(s) be profile function
        // d/ds g(p(s)) = g'(p(s)) * p'(s)
        // d^2/ds^2 = g''(p(s))  * p'(s) * p'(s) + g'(p(s)) * p''(s)
        // how the fuck do u take a derivative of a vector field skull emoji
        val vel = gvfVec * state.v
    }
}