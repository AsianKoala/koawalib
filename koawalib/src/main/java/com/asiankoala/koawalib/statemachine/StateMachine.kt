package com.asiankoala.koawalib.statemachine

import com.asiankoala.koawalib.statemachine.transition.TimedTransition

class StateMachine<StateEnum>(private val stateList: List<State<StateEnum>>, private val universals: List<() -> Unit>) {
    var running = false
        private set

    private var currentState = stateList.first()

    fun start() {
        running = true

        if (currentState.transitionCondition is TimedTransition)
            (currentState.transitionCondition as TimedTransition).startTimer()

        currentState.enterActions.forEach { it.invoke() }
    }

    fun stop() {
        running = false
    }

    fun reset() {
        currentState = stateList.first()
    }

    val state: StateEnum get() = currentState.state

    fun update() {
        if (currentState.transitionCondition.invoke())
            transition()

        if (!running) return

        currentState.loopActions.forEach { it.invoke() }
        universals.forEach { it.invoke() }
    }

    private fun transition() {
        currentState.exitActions.forEach { it.invoke() }

        if (stateList.last() == currentState) {
            running = false
        } else {
            currentState = stateList[stateList.indexOf(currentState) + 1]
        }

        currentState.enterActions.forEach { it.invoke() }

        if (currentState.transitionCondition is TimedTransition)
            (currentState.transitionCondition as TimedTransition).startTimer()
    }
}
