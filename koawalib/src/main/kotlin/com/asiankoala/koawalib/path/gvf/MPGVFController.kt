package com.asiankoala.koawalib.path.gvf

import com.asiankoala.koawalib.control.profile.MotionConstraints
import com.asiankoala.koawalib.control.profile.MotionProfile
import com.asiankoala.koawalib.control.profile.MotionState
import com.asiankoala.koawalib.math.Pose
import com.asiankoala.koawalib.math.Vector
import com.asiankoala.koawalib.math.angleWrap
import com.asiankoala.koawalib.math.degrees
import com.asiankoala.koawalib.path.Path
import com.asiankoala.koawalib.util.Speeds
import com.qualcomm.robotcore.util.ElapsedTime
import kotlin.math.PI
import kotlin.math.sign

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
    private val path: Path,
    private val kN: Double,
    private val kOmega: Double,
    private val kF: Double,
    private val kS: Double,
    private val constraints: MotionConstraints,
    private val epsilon: Double,
    private val errorMap: (Double) -> Double = { it },
) {
    var isFinished = false
        private set

    private val timer = ElapsedTime()
    private val profile: MotionProfile

    private var pose: Pose = Pose()
    private var s: Double = 0.0
    private var gvfVec = Vector()
    private var tangent = Vector()
    private var headingResult = Pair(0.0, 0.0)
    private var vectorResult = Vector()

    fun update(currPose: Pose): Speeds {
        pose = currPose
        s = path.project(pose.vec, s)
        gvfVec = gvfVecAt(pose, s).unit
        headingResult = headingControl()
        vectorResult = vectorControl()
        isFinished = path.length - s < epsilon && pose.vec.dist(path.end.vec) < epsilon
        val speeds = Speeds()
        speeds.setFieldCentric(Pose(vectorResult, headingResult.first))
        return speeds
    }

    private fun gvfVecAt(currPose: Pose, currS: Double): Vector {
        tangent = path[currS, 1].vec
        val normal = tangent.rotate(PI / 2.0)
        val displacementVec = path[currS].vec - currPose.vec
        val error = displacementVec.norm * (displacementVec cross tangent).sign
        return tangent - normal * kN * errorMap.invoke(error)
    }

    private fun headingControl(): Pair<Double, Double> {
        val error = (path[s].heading - pose.heading).angleWrap.degrees
        val result = kOmega * error
        return Pair(result, error)
    }

    private fun vectorControl(): Vector {
        var raw = gvfVec * kS // this is <= 1.0 normalized
        // so im going to assume 1.0 power is just max velocity constraint
        // honestly this is pretty shit and cringe im going to change it
        // when im free from tj workload feelsbadman
        val scaled = profile[timer.seconds()].v / constraints.maxV
        raw *= scaled
        return raw
    }

    init {
        require(kS <= 1.0)
        profile = MotionProfile.generateTrapezoidal(MotionState(), MotionState(path.length), constraints)
    }
}
