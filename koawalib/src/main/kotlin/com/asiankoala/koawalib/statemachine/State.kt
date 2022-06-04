package com.asiankoala.koawalib.statemachine

internal data class State<StateEnum>(
    var state: StateEnum,
    var enterActions: MutableList<() -> Unit>,
    var exitActions: MutableList<() -> Unit>,
    var loopActions: MutableList<() -> Unit>,
    var transitionCondition: () -> Boolean
)
