package com.asiankoala.koawalib.path

import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.math.degrees
import com.asiankoala.koawalib.util.Speeds
import com.asiankoala.koawalib.control.profile.MotionConstraints
import com.asiankoala.koawalib.control.profile.MotionProfile
import com.asiankoala.koawalib.control.profile.MotionState
import com.qualcomm.robotcore.util.ElapsedTime
import kotlin.math.min

/**
 *  Guided Vector Field follower
 *  Uses roadrunner path generation internally cause im lazy
 *  @link https://arxiv.org/pdf/1610.04391.pdf
 *  @param path roadrunner path
 *  @param kN normal path attraction
 *  @param kOmega heading weight
 *  @param kF end param weight
 *  @param kS raw scalar on translational power
 *  @param constraints motion constraints
 *  @param epsilon allowed absolute and projected error
 *  @param errorMap error map to transform normal displacement error
 *  @property isFinished path finish state
 *  honestly this probably isn't the proper way to do a motion profiled gvf,
 *  but i cba to find gradients. doing it this way is just pisseasy and works even if 
 *  (although idk if it works xd)
 *  in the future i might try doing a better impl of motion profiling a gvf
 *  which means either reading 
 *  its half-assed
 */
class MPGVFController(
    path: Path,
    kN: Double,
    kOmega: Double,
    private val kF: Double,
    private val kS: Double,
    private val constraints: MotionConstraints,
    epsilon: Double,
    errorMap: (Double) -> Double = { it },
) : GVFController(path, kN, kOmega, epsilon, errorMap) {
    private val timer = ElapsedTime()
    private val profile: MotionProfile

    override fun headingControl(vel: Speeds): Pair<Double, Double> {
        val error = (tangent.angle - pose.heading).angleWrap.degrees
        val result = kOmega * error
        return Pair(result, error)
    }

    override fun vectorControl(vel: Speeds): Vector {
        var raw = gvfVec * kS * min(1.0, (path.length - s) / kF) // this is <= 1.0 normalized
        // so im going to assume 1.0 power is just max velocity constraint
        // honestly this is pretty shit and cringe im going to change it
        // when im free from tj workload feelsbadman
        val scaled = profile[timer.seconds()].v / constraints.cruiseVel
        raw *= scaled
        return raw
    }

    init {
        require(kS <= 1.0)
        profile = MotionProfile(MotionState(), MotionState(path.length), constraints)
    }
}
