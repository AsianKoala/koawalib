package com.asiankoala.koawalib.util.internal.statemachine

internal class StateMachineBuilder<StateEnum> {
    private val stateList = mutableListOf<State<StateEnum>>()
    private val universals = mutableListOf<() -> Unit>()

    fun state(state: StateEnum): StateMachineBuilder<StateEnum> {
        stateList.add(State(state, mutableListOf(), mutableListOf()) { true })
        return this
    }

    fun transition(transitionCondition: () -> Boolean): StateMachineBuilder<StateEnum> {
        stateList.last().transitionCondition = transitionCondition
        return this
    }

    fun onEnter(action: () -> Unit): StateMachineBuilder<StateEnum> {
        stateList.last().enterActions.add(action)
        return this
    }

    fun loop(action: () -> Unit): StateMachineBuilder<StateEnum> {
        stateList.last().loopActions.add(action)
        return this
    }

    fun universal(action: () -> Unit): StateMachineBuilder<StateEnum> {
        universals.add(action)
        return this
    }

    fun build() = StateMachine(stateList, universals)
}
