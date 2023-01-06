package com.asiankoala.koawalib.util.internal.statemachine

internal data class State<StateEnum>(
    var state: StateEnum,
    var enterActions: MutableList<() -> Unit>,
    var loopActions: MutableList<() -> Unit>,
    var transitionCondition: () -> Boolean
)
