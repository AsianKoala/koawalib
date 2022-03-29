package com.asiankoala.koawalib.statemachine

class StateMachineBuilder<StateEnum> {

    private val stateList = mutableListOf<State<StateEnum>>()
    private val universals = mutableListOf<() -> Unit>()

    fun state(state: StateEnum): StateMachineBuilder<StateEnum> {
        if (stateList.find { it.state == state } != null)
            throw Error("State already exists in list")

        stateList.add(State(state, mutableListOf(), mutableListOf(), mutableListOf()) { true })
        return this
    }

    fun transition(transitionCondition: () -> Boolean): StateMachineBuilder<StateEnum> {
        if (stateList.isEmpty())
            throw Error("No state to transition from")

        stateList.last().transitionCondition = transitionCondition
        return this
    }

    fun transitionTimed(time: Double): StateMachineBuilder<StateEnum> = transition(TimedTransition(time))

    fun onEnter(action: () -> Unit): StateMachineBuilder<StateEnum> {
        if (stateList.isEmpty())
            throw Error("No state to modify")

        stateList.last().enterActions.add(action)
        return this
    }

    fun onExit(action: () -> Unit): StateMachineBuilder<StateEnum> {
        if (stateList.isEmpty())
            throw Error("No state to modify")

        stateList.last().exitActions.add(action)
        return this
    }

    fun loop(action: () -> Unit): StateMachineBuilder<StateEnum> {
        if (stateList.isEmpty())
            throw Error("No state to modify")

        stateList.last().loopActions.add(action)
        return this
    }

    fun universal(action: () -> Unit): StateMachineBuilder<StateEnum> {
        universals.add(action)

        return this
    }

    fun build(): StateMachine<StateEnum> {
        return StateMachine(stateList, universals)
    }
}
