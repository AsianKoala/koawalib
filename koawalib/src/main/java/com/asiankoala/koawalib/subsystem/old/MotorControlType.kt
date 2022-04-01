package com.asiankoala.koawalib.subsystem.old

/**
 * @see com.asiankoala.koawalib.hardware.motor.KMotorEx
 * @see com.asiankoala.koawalib.control.PIDExController
 * @see com.asiankoala.koawalib.control.MotionProfileController
 */
//@Deprecated("port of koawalib v0, do not use")
enum class MotorControlType {
    OPEN_LOOP,
    POSITION_PID,
    MOTION_PROFILE
}