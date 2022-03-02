package com.asiankoala.koawalib.statemachine

import com.asiankoala.koawalib.statemachine.transition.TransitionCondition

data class State<StateEnum>(
    var state: StateEnum,
    var enterActions: MutableList<Action>,
    var exitActions: MutableList<Action>,
    var loopActions: MutableList<Action>,
    var transitionCondition: TransitionCondition?
)
