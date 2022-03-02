package com.asiankoala.koawalib.statemachine.transition

fun interface TransitionCondition {
    fun shouldTransition(): Boolean
}
